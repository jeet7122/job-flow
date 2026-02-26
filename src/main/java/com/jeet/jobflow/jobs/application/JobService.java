package com.jeet.jobflow.jobs.application;

import com.jeet.jobflow.jobs.domain.Job;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobService {
    List<Job> findAll();
    Optional<Job> findById();
    UUID create(String type, String idempotencyKey);
}
