package com.jeet.jobflow.jobs.api;

import jakarta.validation.constraints.NotBlank;

public record CreateJobRequest(@NotBlank String type, String idempotencyKey) { }
