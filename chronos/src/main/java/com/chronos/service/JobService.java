package com.chronos.service;

import com.chronos.model.ScheduledJob;
import com.chronos.repository.ScheduledJobRepository;
import com.chronos.scheduler.QuartzSchedulerService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final ScheduledJobRepository jobRepository;
    private final QuartzSchedulerService schedulerService;
    private final LogService logService;

    public ScheduledJob createJob(ScheduledJob job) throws SchedulerException {
        if (jobRepository.findByJobName(job.getJobName()).isPresent()) {
            throw new IllegalArgumentException("Job with name '" + job.getJobName() + "' already exists.");
        }

        job.setStatus("SCHEDULED");
        job.setRetryCount(0);
        ScheduledJob saved = jobRepository.save(job);
        schedulerService.scheduleJob(saved);
        logService.log(saved.getId(), "SCHEDULED", "Job scheduled");
        return saved;
    }

    public ScheduledJob updateJob(Long id, ScheduledJob updatedJob) throws SchedulerException {
        ScheduledJob existing = jobRepository.findById(id).orElseThrow();
        existing.setJobName(updatedJob.getJobName());
        existing.setUrl(updatedJob.getUrl());
        existing.setHttpMethod(updatedJob.getHttpMethod());
        existing.setCronExpression(updatedJob.getCronExpression());
        existing.setMaxRetries(updatedJob.getMaxRetries());
        jobRepository.save(existing);
        schedulerService.rescheduleJob(existing);
        logService.log(id, "UPDATED", "Job updated and rescheduled");
        return existing;
    }

    public void cancelJob(String jobName) throws SchedulerException {
        schedulerService.pauseJob(jobName);
        logService.log(null, "PAUSED", "Job paused: " + jobName);
    }

    public void deleteJob(Long id) throws SchedulerException {
        ScheduledJob job = jobRepository.findById(id).orElseThrow();
        schedulerService.deleteJob(job.getJobName());
        jobRepository.delete(job);
        logService.log(id, "DELETED", "Job deleted");
    }
    public void resumeJob(String jobName) throws SchedulerException {
        schedulerService.resumeJob(jobName);
        logService.log(null, "RESUMED", "Job resumed: " + jobName);
    }

    public List<ScheduledJob> getAllJobs() {
        return jobRepository.findAll();
    }

    public ScheduledJob getJobById(Long id) {
        return jobRepository.findById(id).orElseThrow();
    }
}