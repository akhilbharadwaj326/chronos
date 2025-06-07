package com.chronos.service;

import com.chronos.model.ScheduledJob;
import com.chronos.repository.ScheduledJobRepository;
import com.chronos.scheduler.QuartzSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobServiceTest {

    private JobService jobService;
    private ScheduledJobRepository jobRepository;
    private QuartzSchedulerService schedulerService;
    private LogService logService;

    @BeforeEach
    void setUp() {
        jobRepository = mock(ScheduledJobRepository.class);
        schedulerService = mock(QuartzSchedulerService.class);
        logService = mock(LogService.class);
        jobService = new JobService(jobRepository, schedulerService, logService);
    }

    @Test
    void createJob_success() throws SchedulerException {
        ScheduledJob job = ScheduledJob.builder().jobName("job1").cronExpression("* * * * * ?").build();
        ScheduledJob savedJob = ScheduledJob.builder().id(1L).jobName("job1").cronExpression("* * * * * ?").build();

        when(jobRepository.findByJobName("job1")).thenReturn(Optional.empty());
        when(jobRepository.save(any())).thenReturn(savedJob);

        ScheduledJob result = jobService.createJob(job);

        assertEquals(savedJob, result);
        verify(schedulerService).scheduleJob(savedJob);
        verify(logService).log(1L, "SCHEDULED", "Job scheduled");
    }

    @Test
    void createJob_duplicate() {
        ScheduledJob job = ScheduledJob.builder().jobName("job1").build();
        when(jobRepository.findByJobName("job1")).thenReturn(Optional.of(job));

        assertThrows(IllegalArgumentException.class, () -> jobService.createJob(job));
    }

    @Test
    void updateJob_success() throws SchedulerException {
        ScheduledJob oldJob = ScheduledJob.builder().id(1L).jobName("job1").build();
        ScheduledJob newJob = ScheduledJob.builder().jobName("job2").url("url").httpMethod("GET").cronExpression("* * * * * ?").maxRetries(5).build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(oldJob));

        ScheduledJob result = jobService.updateJob(1L, newJob);

        verify(jobRepository).save(oldJob);
        verify(schedulerService).rescheduleJob(oldJob);
        verify(logService).log(1L, "UPDATED", "Job updated and rescheduled");
        assertEquals("job2", result.getJobName());
    }

    @Test
    void deleteJob_success() throws SchedulerException {
        ScheduledJob job = ScheduledJob.builder().id(1L).jobName("job1").build();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.deleteJob(1L);

        verify(schedulerService).deleteJob("job1");
        verify(jobRepository).delete(job);
        verify(logService).log(1L, "DELETED", "Job deleted");
    }

    @Test
    void cancelJob_success() throws SchedulerException {
        jobService.cancelJob("job1");
        verify(schedulerService).pauseJob("job1");
        verify(logService).log(null, "PAUSED", "Job paused: job1");
    }

    @Test
    void resumeJob_success() throws SchedulerException {
        jobService.resumeJob("job1");
        verify(schedulerService).resumeJob("job1");
        verify(logService).log(null, "RESUMED", "Job resumed: job1");
    }

    @Test
    void getAllJobs_shouldCallRepo() {
        jobService.getAllJobs();
        verify(jobRepository).findAll();
    }

    @Test
    void getJobById_shouldCallRepo() {
        ScheduledJob job = new ScheduledJob();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertEquals(job, jobService.getJobById(1L));
    }
}
