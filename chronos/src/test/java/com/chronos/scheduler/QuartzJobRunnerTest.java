package com.chronos.scheduler;

import com.chronos.model.ScheduledJob;
import com.chronos.repository.ScheduledJobRepository;
import com.chronos.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.mockito.Mockito.*;

class QuartzJobRunnerTest {

    @InjectMocks
    private QuartzJobRunner quartzJobRunner;

    @Mock
    private ScheduledJobRepository jobRepository;

    @Mock
    private LogService logService;

    @Mock
    private JobExecutionContext context;

    @Mock
    private JobDetail jobDetail;

    @Spy
    private RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.getJobDetail()).thenReturn(jobDetail);
    }

    @Test
    void testExecuteSuccess() {
        ScheduledJob job = ScheduledJob.builder()
                .id(1L)
                .url("http://localhost")
                .httpMethod("GET")
                .maxRetries(3)
                .retryCount(0)
                .build();

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("jobId", 1L);

        when(jobDetail.getJobDataMap()).thenReturn(dataMap);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        RestTemplate mockTemplate = mock(RestTemplate.class);
        quartzJobRunner = new QuartzJobRunner();
        quartzJobRunner.restTemplate = mockTemplate;
        quartzJobRunner.jobRepository = jobRepository;
        quartzJobRunner.logService = logService;

        when(mockTemplate.exchange(eq(job.getUrl()), eq(HttpMethod.GET), isNull(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        quartzJobRunner.execute(context);

        verify(logService).log(1L, "SUCCESS", "Job executed successfully");
        verify(jobRepository).save(any(ScheduledJob.class));
    }

    @Test
    void testExecuteJobNotFound() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("jobId", 999L);

        when(jobDetail.getJobDataMap()).thenReturn(dataMap);
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        quartzJobRunner.execute(context);

        verify(logService).log(999L, "FAILED", "Job not found in database");
    }

    @Test
    void testExecuteRetryAndFail() {
        ScheduledJob job = ScheduledJob.builder()
                .id(1L)
                .url("http://fail")
                .httpMethod("GET")
                .maxRetries(2)
                .retryCount(1)
                .build();

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("jobId", 1L);

        when(jobDetail.getJobDataMap()).thenReturn(dataMap);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        RestTemplate mockTemplate = mock(RestTemplate.class);
        quartzJobRunner = new QuartzJobRunner();
        quartzJobRunner.restTemplate = mockTemplate;
        quartzJobRunner.jobRepository = jobRepository;
        quartzJobRunner.logService = logService;

        when(mockTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Timeout"));

        quartzJobRunner.execute(context);

        verify(logService).log(eq(1L), eq("FAILED"), contains("Max retries reached"));
        verify(jobRepository).save(any(ScheduledJob.class));
    }
}
