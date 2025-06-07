package com.chronos.contoller;

import com.chronos.controller.JobController;
import com.chronos.model.JobLog;
import com.chronos.model.ScheduledJob;
import com.chronos.service.JobService;
import com.chronos.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobControllerTest {

    private JobController controller;
    private JobService jobService;
    private LogService logService;

    @BeforeEach
    void setup() {
        jobService = mock(JobService.class);
        logService = mock(LogService.class);
        controller = new JobController(jobService, logService);
    }

    @Test
    void testCreate() throws SchedulerException {
        ScheduledJob job = new ScheduledJob();
        when(jobService.createJob(job)).thenReturn(job);

        ResponseEntity<ScheduledJob> response = controller.create(job);

        assertEquals(job, response.getBody());
        verify(jobService).createJob(job);
    }

    @Test
    void testUpdate() throws SchedulerException {
        ScheduledJob job = new ScheduledJob();
        when(jobService.updateJob(1L, job)).thenReturn(job);

        ResponseEntity<ScheduledJob> response = controller.update(1L, job);

        assertEquals(job, response.getBody());
        verify(jobService).updateJob(1L, job);
    }

    @Test
    void testDelete() throws SchedulerException {
        ResponseEntity<Void> response = controller.delete(1L);
        assertEquals(204, response.getStatusCodeValue());
        verify(jobService).deleteJob(1L);
    }

    @Test
    void testCancel() throws SchedulerException {
        ResponseEntity<Void> response = controller.cancel("job");
        assertEquals(200, response.getStatusCodeValue());
        verify(jobService).cancelJob("job");
    }

    @Test
    void testGetLogs() {
        List<JobLog> logs = List.of(new JobLog());
        when(logService.getLogsForJob(1L)).thenReturn(logs);

        ResponseEntity<List<JobLog>> response = controller.getLogs(1L);

        assertEquals(logs, response.getBody());
        verify(logService).getLogsForJob(1L);
    }

    @Test
    void testResumeJob() throws SchedulerException {
        ResponseEntity<String> response = controller.resumeJob("job");
        assertEquals("Job resumed: job", response.getBody());
        verify(jobService).resumeJob("job");
    }
}
