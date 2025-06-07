package com.chronos.dto;

import lombok.Data;

@Data
public class JobRequest {
    private String jobName;
    private String cronExpression;
    private String url;
    private String httpMethod;
    private int maxRetries;
}
