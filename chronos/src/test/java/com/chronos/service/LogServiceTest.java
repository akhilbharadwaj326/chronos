package com.chronos.service;

import com.chronos.model.JobLog;
import com.chronos.repository.JobLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogServiceTest {

    private LogService logService;
    private JobLogRepository logRepository;

    @BeforeEach
    void setUp() {
        logRepository = mock(JobLogRepository.class);
        logService = new LogService(logRepository);
    }

    @Test
    void testLog() {
        ArgumentCaptor<JobLog> captor = ArgumentCaptor.forClass(JobLog.class);

        logService.log(1L, "SUCCESS", "Job ran");

        verify(logRepository).save(captor.capture());
        JobLog log = captor.getValue();

        assertEquals(1L, log.getJobId());
        assertEquals("SUCCESS", log.getStatus());
        assertEquals("Job ran", log.getMessage());
        assertNotNull(log.getTimestamp());
    }

    @Test
    void testGetLogsForJob() {
        List<JobLog> dummyLogs = List.of(new JobLog());
        when(logRepository.findByJobId(1L)).thenReturn(dummyLogs);

        List<JobLog> result = logService.getLogsForJob(1L);

        assertEquals(dummyLogs, result);
        verify(logRepository).findByJobId(1L);
    }
}
