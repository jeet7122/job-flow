package com.jeet.jobflow.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    @Autowired
    private JobServiceImplementation jobServiceImplementation;

    @GetMapping
    public List<Job> getAllJobs(){
        return jobServiceImplementation.findAll();
    }

}
