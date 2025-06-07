package com.chronos.scheduler;

import com.chronos.model.ScheduledJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.quartz.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class QuartzSchedulerServiceTest {

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private QuartzSchedulerService schedulerService;

    private ScheduledJob sampleJob;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleJob = ScheduledJob.builder()
                .id(1L)
                .jobName("TestJob")
                .cronExpression("0/5 * * * * ?")
                .build();
    }

    @Test
    void shouldScheduleJobSuccessfully() throws SchedulerException {
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        schedulerService.scheduleJob(sampleJob);

        verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void shouldThrowWhenJobAlreadyScheduled() throws SchedulerException {
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        assertThrows(SchedulerException.class, () -> schedulerService.scheduleJob(sampleJob));
    }

    @Test
    void shouldDeleteJobIfExists() throws SchedulerException {
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        schedulerService.deleteJob(sampleJob.getJobName());

        verify(scheduler).deleteJob(new JobKey(sampleJob.getJobName()));
    }

    @Test
    void shouldRescheduleJob() throws SchedulerException {
        schedulerService.rescheduleJob(sampleJob);

        verify(scheduler).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test
    void shouldPauseJobAndTrigger() throws SchedulerException {
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);
        when(scheduler.checkExists(any(TriggerKey.class))).thenReturn(true);

        schedulerService.pauseJob(sampleJob.getJobName());

        verify(scheduler).pauseJob(any(JobKey.class));
        verify(scheduler).pauseTrigger(any(TriggerKey.class));
    }

    @Test
    void shouldThrowIfPauseJobNotFound() throws SchedulerException {
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        assertThrows(SchedulerException.class, () -> schedulerService.pauseJob(sampleJob.getJobName()));
    }

    @Test
    void shouldResumeJob() throws SchedulerException {
        schedulerService.resumeJob(sampleJob.getJobName());

        verify(scheduler).resumeJob(any(JobKey.class));
        verify(scheduler).resumeTrigger(any(TriggerKey.class));
    }
}
