package com.chronos.service;

import com.chronos.model.JobLog;
import com.chronos.repository.JobLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {

    private final JobLogRepository logRepository;

    public void log(Long jobId, String status, String message) {
        JobLog log = JobLog.builder()
                .jobId(jobId)
                .status(status)
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        logRepository.save(log);
    }

    public List<JobLog> getLogsForJob(Long jobId) {
        return logRepository.findByJobId(jobId);
    }
}
