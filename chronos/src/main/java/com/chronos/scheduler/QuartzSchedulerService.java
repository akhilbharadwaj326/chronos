package com.chronos.scheduler;

import com.chronos.model.ScheduledJob;
import com.chronos.scheduler.QuartzJobRunner;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuartzSchedulerService {

    private final Scheduler scheduler;
    public void scheduleJob(ScheduledJob job) throws SchedulerException {
        JobKey jobKey = new JobKey(job.getJobName());

        if (scheduler.checkExists(jobKey)) {
            throw new SchedulerException("Job '" + job.getJobName() + "' is already scheduled.");
        }

        JobDetail jobDetail = JobBuilder.newJob(QuartzJobRunner.class)
                .withIdentity(jobKey)
                .usingJobData("jobId", job.getId())
                .build();

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getJobName() + "_trigger")
                .withSchedule(scheduleBuilder)
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void deleteJob(String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
    }

    public void rescheduleJob(ScheduledJob job) throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey(job.getJobName() + "_trigger");

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(scheduleBuilder)
                .build();

        scheduler.rescheduleJob(triggerKey, newTrigger);
    }

    public void pauseJob(String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);

            // Also pause associated triggers
            TriggerKey triggerKey = new TriggerKey(jobName + "_trigger");
            if (scheduler.checkExists(triggerKey)) {
                scheduler.pauseTrigger(triggerKey);
            }
        } else {
            throw new SchedulerException("Job with name " + jobName + " does not exist");
        }
    }
    public void resumeJob(String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        scheduler.resumeJob(jobKey);
        scheduler.resumeTrigger(new TriggerKey(jobName + "_trigger"));
    }
}


