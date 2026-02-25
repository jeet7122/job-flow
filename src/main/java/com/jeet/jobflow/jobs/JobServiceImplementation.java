package com.jeet.jobflow.jobs;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobServiceImplementation implements JobService{
    private final JobRepository jobRepository;
    public JobServiceImplementation(JobRepository jobRepository){
        this.jobRepository = jobRepository;
    }
    @Override
    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    @Override
    public Optional<Job> findById() {
        return Optional.empty();
    }
}
