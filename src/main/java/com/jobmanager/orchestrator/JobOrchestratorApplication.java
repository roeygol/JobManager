package com.jobmanager.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class JobOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobOrchestratorApplication.class, args);
    }
}

