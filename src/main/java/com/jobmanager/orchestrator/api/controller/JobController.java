package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.JobExecutionRequest;
import com.jobmanager.orchestrator.api.dto.JobExecutionResponse;
import com.jobmanager.orchestrator.api.dto.JobStatusResponse;
import com.jobmanager.orchestrator.api.mapper.JobStatusMapper;
import com.jobmanager.orchestrator.application.service.JobOrchestrationService;
import com.jobmanager.orchestrator.domain.entity.JobStatus;
import com.jobmanager.orchestrator.domain.exception.JobMappingNotFoundException;
import com.jobmanager.orchestrator.domain.exception.JobNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for job orchestration operations.
 * Exposes endpoints for job execution and status polling.
 */
@RestController
@RequestMapping("/jobs")
@Tag(name = "Job Orchestration", description = "API for job execution and status tracking")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final JobOrchestrationService orchestrationService;
    private final JobStatusMapper statusMapper;

    public JobController(JobOrchestrationService orchestrationService, JobStatusMapper statusMapper) {
        this.orchestrationService = orchestrationService;
        this.statusMapper = statusMapper;
    }

    @PostMapping
    @Operation(summary = "Create and trigger a job execution", 
               description = "Creates a new job execution and triggers it asynchronously")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job execution created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Job mapping not found")
    })
    public ResponseEntity<JobExecutionResponse> createJob(@Valid @RequestBody JobExecutionRequest request) {
        logger.info("Received job execution request for job name: {}", request.getJobName());

        try {
            UUID executionUuid = orchestrationService.createAndTriggerJob(request.getJobName());
            logger.info("Job execution created with UUID: {}", executionUuid);

            return ResponseEntity.ok(new JobExecutionResponse(executionUuid));

        } catch (JobMappingNotFoundException e) {
            logger.warn("Job mapping not found: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get job execution status", 
               description = "Retrieves the current status of a job execution by UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<JobStatusResponse> getJobStatus(
            @Parameter(description = "Job execution UUID", required = true)
            @PathVariable UUID uuid) {
        logger.debug("Received status request for UUID: {}", uuid);

        JobStatus jobStatus = orchestrationService.getJobStatus(uuid);
        JobStatusResponse response = statusMapper.toDto(jobStatus);

        return ResponseEntity.ok(response);
    }
}

