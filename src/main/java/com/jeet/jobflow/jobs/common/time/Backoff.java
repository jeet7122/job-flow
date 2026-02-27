package com.jeet.jobflow.jobs.common.time;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public final class Backoff {
    private Backoff(){}


    public static Duration compute(int attempt, Duration base, Duration max){
        long expSeconds = (long) (base.getSeconds() * Math.pow(2, Math.max(0, attempt - 1)));

        long jitter = (long) (expSeconds * ThreadLocalRandom.current().nextDouble(0.0, 0.2));

        long total = expSeconds + jitter;
        long capped = Math.min(total, max.getSeconds());
        return Duration.ofSeconds(Math.max(1, capped));
    }
}
