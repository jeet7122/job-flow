package com.jeet.jobflow.jobs.worker;

import com.jeet.jobflow.jobs.JobRepository;
import com.jeet.jobflow.jobs.domain.Job;
import com.jeet.jobflow.jobs.domain.JobStatus;
import com.jeet.jobflow.jobs.infra.JobQueueProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 *  ---------------- JOB WORKER Extends Command Line Runner ------------------/
 *  ----------------- Executes the queued job -------------------------------/
 *  #run() runs
 */
@Component
@ConditionalOnProperty(
        name = "jobflow.worker.enabled",
        havingValue = "true"
)
public class JobWorker implements CommandLineRunner {
    private final StringRedisTemplate redisTemplate;
    private final JobRepository repository;

    @Value("${jobflow.worker.id}")
    private String workerId;

    @Value("${jobflow.worker.lease-seconds:60}")
    private long leaseSeconds;

    public JobWorker(StringRedisTemplate redisTemplate, JobRepository repository){
        this.redisTemplate = redisTemplate;
        this.repository = repository;
    }


    @Override
    public void run(String... args) throws Exception {
        while (true){
            var res = redisTemplate.opsForList().rightPop(JobQueueProducer.QUEUE_KEY, Duration.ofSeconds(30));
            if(res == null) continue;
            UUID jobId = UUID.fromString(res);
            System.out.println("Worker picked job: " + res);
            process(jobId);
        }
    }

    @Transactional
    public void process(UUID jobId){
        Instant now = Instant.now();
        Instant lockedUntil = now.plusSeconds(leaseSeconds);

        int locked = repository.tryLockQueuedJob(
                jobId,
                JobStatus.QUEUED,
                JobStatus.RUNNING,
                workerId,
                lockedUntil,
                now
        );

        if (locked == 0) return;

        Optional<Job> optionalJob = repository.findById(jobId);
        if (optionalJob.isEmpty()) return;
        Job job = optionalJob.get();

        if (!workerId.equals(job.getLockedBy())) return;

        try{
            System.out.println("Worker is processing job: " + jobId);
            sleep(200);
            job.setStatus(JobStatus.SUCCEEDED);
            job.setLockedBy(null);
            job.setLockedUntil(null);
            job.setUpdatedAt(Instant.now());
            repository.save(job);
        }
        catch (Exception e){
            job.setStatus(JobStatus.FAILED);
            job.setLastError(e.getMessage());
            job.setLockedBy(null);
            job.setLockedUntil(null);
            job.setUpdatedAt(Instant.now());
            repository.save(job);
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
