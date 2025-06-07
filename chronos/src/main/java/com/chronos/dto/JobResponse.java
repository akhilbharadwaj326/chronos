package com.chronos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String jobName;
    private String status;
}
