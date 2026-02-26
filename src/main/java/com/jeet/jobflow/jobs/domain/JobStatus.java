package com.jeet.jobflow.jobs.domain;

public enum JobStatus {
    QUEUED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    RETRY_SCHEDULED,
    DEAD_LETTER
}
