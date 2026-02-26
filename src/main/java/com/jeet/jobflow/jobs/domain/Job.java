package com.jeet.jobflow.jobs.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Data
public class Job {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private JobStatus status;
    private String type;
    private Integer priority;
    private Integer attempt;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "run_at")
    private Instant runAt;

    @Column(name = "locked_by")
    private String lockedBy;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
