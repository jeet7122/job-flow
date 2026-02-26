package com.jeet.jobflow.jobs.api;

import com.jeet.jobflow.jobs.application.JobServiceImplementation;
import com.jeet.jobflow.jobs.domain.Job;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    @Autowired
    private JobServiceImplementation jobServiceImplementation;

    @GetMapping
    public List<Job> getAllJobs(){
        return jobServiceImplementation.findAll();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UUID create(@RequestBody @Valid CreateJobRequest request){
        return jobServiceImplementation.create(request.type(), request.idempotencyKey());
    }

}
