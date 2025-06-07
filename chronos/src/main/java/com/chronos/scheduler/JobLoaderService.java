package com.chronos.scheduler;

import com.chronos.model.ScheduledJob;
import com.chronos.repository.ScheduledJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobLoaderService {

    private final ScheduledJobRepository scheduledJobRepository;
    private final Scheduler scheduler;

    private static final String JOB_GROUP = "DEFAULT";
    private static final String TRIGGER_GROUP = "DEFAULT";

    @PostConstruct
    public void scheduleExistingJobs() {
        List<ScheduledJob> jobs = scheduledJobRepository.findAll();

        for (ScheduledJob job : jobs) {
            try {
                JobKey jobKey = new JobKey(job.getJobName(), JOB_GROUP);
                if (scheduler.checkExists(jobKey)) {
                    log.info("Job '{}' already exists, skipping.", job.getJobName());
                    continue;
                }

                JobDetail jobDetail = JobBuilder.newJob(QuartzJobRunner.class)
                        .withIdentity(jobKey)
                        .usingJobData("jobId", String.valueOf(job.getId()))
                        .storeDurably()
                        .build();

                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(job.getJobName() + "_trigger", TRIGGER_GROUP)
                        .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                        .forJob(jobDetail)
                        .build();

                scheduler.scheduleJob(jobDetail, trigger);
                log.info("Scheduled job at startup: '{}'", job.getJobName());

            } catch (SchedulerException e) {
                log.error("Failed to schedule job '{}' on startup: {}", job.getJobName(), e.getMessage());
            }
        }
    }
}
