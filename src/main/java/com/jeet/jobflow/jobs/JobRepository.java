package com.jeet.jobflow.jobs;

import com.jeet.jobflow.jobs.domain.Job;
import com.jeet.jobflow.jobs.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    @Modifying
    @Transactional
    @Query("""
    update Job j
       set j.status = :running,
           j.lockedBy = :workerId,
           j.lockedUntil = :lockedUntil,
           j.updatedAt = :now
     where j.id = :jobId
       and j.status = :queued
       and (j.lockedUntil is null or j.lockedUntil < :now)
  """)
    int tryLockQueuedJob(
            @Param("jobId") UUID jobId,
            @Param("queued") JobStatus queued,
            @Param("running") JobStatus running,
            @Param("workerId") String workerId,
            @Param("lockedUntil") Instant lockedUntil,
            @Param("now") Instant now
    );


    List<Job> findTop100ByStatusAndRunAtLessThanEqualOrderByRunAtAsc(JobStatus status, Instant now);
}
