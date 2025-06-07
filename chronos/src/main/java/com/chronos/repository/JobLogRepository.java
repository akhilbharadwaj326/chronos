package com.chronos.repository;

import com.chronos.model.JobLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobLogRepository extends JpaRepository<JobLog, Long> {
    List<JobLog> findByJobId(Long jobId);
}
