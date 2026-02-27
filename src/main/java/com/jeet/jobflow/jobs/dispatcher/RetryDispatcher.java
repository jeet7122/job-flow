package com.jeet.jobflow.jobs.dispatcher;

import com.jeet.jobflow.jobs.JobRepository;
import com.jeet.jobflow.jobs.domain.Job;
import com.jeet.jobflow.jobs.domain.JobStatus;
import com.jeet.jobflow.jobs.infra.JobQueueProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class RetryDispatcher {
    private final JobRepository repository;
    private final JobQueueProducer producer;

    public RetryDispatcher(JobRepository repository, JobQueueProducer producer){
        this.repository = repository;
        this.producer = producer;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void dispatchDueRetries(){
        List<Job> due = repository.findTop100ByStatusAndRunAtLessThanEqualOrderByRunAtAsc(JobStatus.RETRY_SCHEDULED, Instant.now());

        for (Job dj : due){
            dj.setStatus(JobStatus.QUEUED);
            dj.setRunAt(null);
            dj.setUpdatedAt(Instant.now());
            repository.save(dj);
            producer.enqueue(dj.getId());
        }
    }
}
