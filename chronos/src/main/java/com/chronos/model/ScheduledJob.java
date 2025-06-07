package com.chronos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String jobName;
    private String cronExpression;
    private String url;
    private String httpMethod; // GET or POST
    private int maxRetries;
    private int retryCount;
    private String status;
    private LocalDateTime nextRun;
}
