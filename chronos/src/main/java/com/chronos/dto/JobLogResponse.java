package com.chronos.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobLogResponse {
    private Long id;
    private String jobName;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}
