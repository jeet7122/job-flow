package com.jeet.jobflow.jobs.application;

import com.jeet.jobflow.jobs.domain.Job;
import com.jeet.jobflow.jobs.JobRepository;
import com.jeet.jobflow.jobs.domain.JobStatus;
import com.jeet.jobflow.jobs.infra.JobQueueProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobServiceImplementation implements JobService {
    private final JobRepository jobRepository;
    @Autowired
    private JobQueueProducer queueProducer;


    public JobServiceImplementation(JobRepository jobRepository) {
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

    @Override
    @Transactional
    public UUID create(String type, String idempotencyKey) {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setType(type);
        job.setStatus(JobStatus.QUEUED);
        job.setPriority(0);
        job.setIdempotencyKey(idempotencyKey);
        job.setAttempt(0);
        job.setMaxAttempts(5);
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);

        queueProducer.enqueue(job.getId());
        return job.getId();
    }


}
