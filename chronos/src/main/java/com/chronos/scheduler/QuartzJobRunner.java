package com.chronos.scheduler;

import com.chronos.model.ScheduledJob;
import com.chronos.repository.ScheduledJobRepository;
import com.chronos.service.LogService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class QuartzJobRunner implements Job {

    @Autowired
    ScheduledJobRepository jobRepository;

    @Autowired
    LogService logService;

    RestTemplate restTemplate = new RestTemplate();

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap map = context.getJobDetail().getJobDataMap();
//        String jobIdStr = map.getString("jobId");
//        Long jobId = null;
//
//        try {
//            jobId = Long.valueOf(jobIdStr);
//        } catch (NumberFormatException e) {
//            logService.log(null, "FAILED", "Invalid jobId in JobDataMap: " + jobIdStr);
//            return;
//        }
        Object jobIdObj = map.get("jobId");
        Long jobId = null;

        if (jobIdObj instanceof String) {
            jobId = Long.valueOf((String) jobIdObj);
        } else if (jobIdObj instanceof Number) {
            jobId = ((Number) jobIdObj).longValue();
        } else {
            logService.log(null, "FAILED", "Invalid jobId type in JobDataMap: " + jobIdObj);
            return;
        }
        ScheduledJob job = jobRepository.findById(jobId).orElse(null);

        if (job == null) {
            logService.log(jobId, "FAILED", "Job not found in database");
            return;
        }

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    job.getUrl(),
                    HttpMethod.valueOf(job.getHttpMethod()),
                    null,
                    String.class
            );

            job.setStatus("SUCCESS");
            logService.log(jobId, "SUCCESS", "Job executed successfully");

        } catch (Exception e) {
            job.setRetryCount(job.getRetryCount() + 1);
            if (job.getRetryCount() >= job.getMaxRetries()) {
                job.setStatus("FAILED");
                logService.log(jobId, "FAILED", "Max retries reached: " + e.getMessage());
            } else {
                job.setStatus("RETRYING");
                logService.log(jobId, "RETRYING", "Attempt " + job.getRetryCount() + ": " + e.getMessage());
            }
        }

        jobRepository.save(job);
    }
}
