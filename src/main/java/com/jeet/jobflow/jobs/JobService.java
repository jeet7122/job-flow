package com.jeet.jobflow.jobs;

import java.util.List;
import java.util.Optional;

public interface JobService {
    List<Job> findAll();
    Optional<Job> findById();
}
