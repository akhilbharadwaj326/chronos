package com.chronos.controller;

import com.chronos.model.JobLog;
import com.chronos.model.ScheduledJob;
import com.chronos.service.JobService;
import com.chronos.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final LogService logService;

    @PostMapping
    @Operation(summary = "Schedule a new job")
    public ResponseEntity<ScheduledJob> create(@RequestBody ScheduledJob job) throws SchedulerException {
        return ResponseEntity.ok(jobService.createJob(job));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a job")
    public ResponseEntity<ScheduledJob> update(@PathVariable Long id, @RequestBody ScheduledJob job) throws SchedulerException {
        return ResponseEntity.ok(jobService.updateJob(id, job));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws SchedulerException {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{name}/cancel")
    @Operation(summary = "Cancel a job")
    public ResponseEntity<Void> cancel(@PathVariable String name) throws SchedulerException {
        jobService.cancelJob(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "Get job logs")
    public ResponseEntity<List<JobLog>> getLogs(@PathVariable Long id) {
        return ResponseEntity.ok(logService.getLogsForJob(id));
    }
    @PutMapping("/resume/{jobName}")
    public ResponseEntity<String> resumeJob(@PathVariable String jobName) throws SchedulerException {
        jobService.resumeJob(jobName);
        return ResponseEntity.ok("Job resumed: " + jobName);
    }

}
