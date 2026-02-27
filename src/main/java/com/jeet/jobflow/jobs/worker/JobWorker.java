package com.jeet.jobflow.jobs.worker;

import com.jeet.jobflow.jobs.JobRepository;
import com.jeet.jobflow.jobs.common.time.Backoff;
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
            String res = redisTemplate.opsForList().rightPopAndLeftPush(JobQueueProducer.QUEUE_KEY,JobQueueProducer.PROCESSING_KEY ,Duration.ofSeconds(30));
            if(res == null) continue;
            UUID jobId = UUID.fromString(res);
            System.out.println("Worker picked job: " + res);
            process(jobId, res);
            ackProcessing(res);
        }
    }

    @Transactional
    public void process(UUID jobId, String redisVal){
        System.out.println("Worker is Running Job: " + jobId);
        Instant now = Instant.now();
        Instant lockedUntil = now.plusSeconds(leaseSeconds);

        System.out.println("Before try lock");
        int locked = repository.tryLockQueuedJob(
                jobId,
                JobStatus.QUEUED,
                JobStatus.RUNNING,
                workerId,
                lockedUntil,
                now
        );
        System.out.println("After Try Lock: " + locked);

        if (locked == 0) {
            var opt = repository.findById(jobId);
            if (opt.isPresent() && opt.get().getStatus() == JobStatus.QUEUED) {
                redisTemplate.opsForList().leftPush(JobQueueProducer.QUEUE_KEY, jobId.toString());
                sleep(50);
            } else {
                // drop it from queue - it's not runnable anymore
                System.out.println("Dropping non-QUEUED jobId from queue: " + jobId);
            }
            return;
        }

        Optional<Job> optionalJob = repository.findById(jobId);
        if (optionalJob.isEmpty()){
            ackProcessing(redisVal);
            return;
        }
        Job job = optionalJob.get();

        if (!workerId.equals(job.getLockedBy())) {
            ackProcessing(redisVal);
            return;
        }

        try{
            System.out.println("Worker is processing job: " + jobId);
            sleep(200);

            job.setStatus(JobStatus.SUCCEEDED);
            job.setLockedBy(null);
            job.setLockedUntil(null);
            job.setUpdatedAt(Instant.now());
            repository.save(job);
            ackProcessing(redisVal);
        }
        catch (Exception e){
            job.setLastError(e.getMessage());
            job.setAttempt(job.getAttempt() + 1);
            job.setUpdatedAt(Instant.now());

            if (job.getAttempt() < job.getMaxAttempts()){
                Duration delay = Backoff.compute(job.getAttempt(), Duration.ofSeconds(2), Duration.ofMinutes(5));
                job.setStatus(JobStatus.RETRY_SCHEDULED);
                job.setRunAt(Instant.now().plus(delay));
            }
            else {
                job.setStatus(JobStatus.DEAD_LETTER);
            }
            job.setLockedBy(null);
            job.setLockedUntil(null);
            repository.save(job);
            ackProcessing(redisVal);
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void ackProcessing(String redisVal){
        redisTemplate.opsForList().remove(JobQueueProducer.PROCESSING_KEY, 1, redisVal);
    }

    private void requeueFromProcessing(String redisVal){
        ackProcessing(redisVal);
        redisTemplate.opsForList().leftPush(JobQueueProducer.QUEUE_KEY, redisVal);
        sleep(50);
    }
}
