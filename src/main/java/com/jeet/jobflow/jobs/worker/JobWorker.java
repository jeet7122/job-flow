package com.jeet.jobflow.jobs.worker;

import com.jeet.jobflow.jobs.JobRepository;
import com.jeet.jobflow.jobs.domain.Job;
import com.jeet.jobflow.jobs.domain.JobStatus;
import com.jeet.jobflow.jobs.infra.JobQueueProducer;
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
        Optional<Job> optionalJob = repository.findById(jobId);
        if (optionalJob.isEmpty()) return;
        Job job = optionalJob.get();
        if (job.getStatus() != JobStatus.QUEUED) return;
        job.setStatus(JobStatus.RUNNING);
        job.setUpdatedAt(Instant.now());
        repository.save(job);

        try{
            sleep(200);
            System.out.println("Worker is processing job" + jobId);
            job.setStatus(JobStatus.SUCCEEDED);
            job.setUpdatedAt(Instant.now());
            repository.save(job);
        }
        catch (Exception e){
            job.setStatus(JobStatus.FAILED);
            job.setLastError(e.getMessage());
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
