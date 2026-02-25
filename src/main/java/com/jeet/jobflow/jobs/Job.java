package com.jeet.jobflow.jobs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    private UUID id;
    private String type;
    private Integer priority;
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
