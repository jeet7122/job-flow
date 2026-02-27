package com.jeet.jobflow.jobs.infra;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobQueueProducer {

    public static final String QUEUE_KEY = "jobs.queue";
    public static final String PROCESSING_KEY = "jobs.queue";
    private final StringRedisTemplate redisTemplate;
    public JobQueueProducer(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    public void enqueue(UUID jobId){
        redisTemplate.opsForList().leftPush(QUEUE_KEY,jobId.toString());
    }
}
