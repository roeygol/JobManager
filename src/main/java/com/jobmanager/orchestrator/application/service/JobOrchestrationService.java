package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.domain.entity.JobStatus;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import com.jobmanager.orchestrator.persistence.repository.JobStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Core orchestration service responsible for job execution lifecycle management.
 * Coordinates mapping resolution, remote execution, and state persistence.
 */
@Service
public class JobOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(JobOrchestrationService.class);

    private final JobMappingService mappingService;
    private final RemoteJobClient remoteJobClient;
    private final JobStatusRepository jobStatusRepository;

    public JobOrchestrationService(
            JobMappingService mappingService,
            RemoteJobClient remoteJobClient,
            JobStatusRepository jobStatusRepository) {
        this.mappingService = mappingService;
        this.remoteJobClient = remoteJobClient;
        this.jobStatusRepository = jobStatusRepository;
    }

    /**
     * Creates and triggers a job execution asynchronously.
     *
     * @param jobName the job name to execute
     * @return the UUID of the created job execution
     */
    @Transactional
    public UUID createAndTriggerJob(String jobName) {
        logger.info("Creating job execution for job name: {}", jobName);

        // Resolve mapping
        JobRestMapping mapping = mappingService.resolveMapping(jobName);
        logger.debug("Resolved mapping: {} -> {}:{}", jobName, mapping.getServiceName(), mapping.getFullEndpointUrl());

        // Create job status entity
        UUID executionUuid = UUID.randomUUID();
        JobStatus jobStatus = new JobStatus(executionUuid, JobExecutionStatus.STARTED);
        jobStatus = jobStatusRepository.save(jobStatus);
        logger.info("Created job status with UUID: {}", executionUuid);

        // Trigger asynchronous execution
        executeJobAsync(jobStatus, mapping);

        return executionUuid;
    }

    /**
     * Executes the job asynchronously and updates status.
     * Note: Entity must be reloaded in async context to avoid detached entity issues.
     */
    @Async("jobExecutor")
    @Transactional
    public CompletableFuture<Void> executeJobAsync(JobStatus jobStatus, JobRestMapping mapping) {
        UUID uuid = jobStatus.getUuid();
        logger.info("Starting asynchronous execution for job UUID: {}", uuid);

        try {
            // Reload entity in async context to ensure it's managed
            JobStatus managedStatus = jobStatusRepository.findByUuid(uuid)
                    .orElseThrow(() -> new IllegalStateException("Job status not found for UUID: " + uuid));

            // Update status to IN_PROGRESS
            managedStatus.setStatus(JobExecutionStatus.IN_PROGRESS);
            managedStatus = jobStatusRepository.save(managedStatus);
            logger.debug("Updated job status to IN_PROGRESS for UUID: {}", uuid);

            // Execute remote call
            String endpointUrl = mapping.getFullEndpointUrl();
            String jobName = mapping.getJobName();
            logger.info("Executing remote call to endpoint: {} for job: {}", endpointUrl, jobName);
            
            // Create request body with job name
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("jobName", jobName);
            
            RemoteJobClient.RemoteJobResponse response = remoteJobClient.executeJob(endpointUrl, requestBody);

            // Update status based on response
            managedStatus.setResponse(response.getResponseBody());
            managedStatus.setHttpStatus(response.getHttpStatus());
            managedStatus.setEndDate(LocalDateTime.now());

            if (response.isSuccess()) {
                managedStatus.setStatus(JobExecutionStatus.SUCCESS);
                logger.info("Job execution completed successfully for UUID: {} with HTTP status: {}", 
                        uuid, response.getHttpStatus());
            } else {
                managedStatus.setStatus(JobExecutionStatus.FAILED);
                logger.warn("Job execution failed for UUID: {} with HTTP status: {}", 
                        uuid, response.getHttpStatus());
            }

            jobStatusRepository.save(managedStatus);

        } catch (Exception e) {
            logger.error("Exception during job execution for UUID: {}", uuid, e);
            
            // Reload entity to update in case of error
            jobStatusRepository.findByUuid(uuid).ifPresent(status -> {
                status.setStatus(JobExecutionStatus.FAILED);
                status.setResponse("Execution error: " + e.getMessage());
                status.setHttpStatus(0);
                status.setEndDate(LocalDateTime.now());
                jobStatusRepository.save(status);
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Retrieves job status by UUID.
     *
     * @param uuid the job execution UUID
     * @return the JobStatus entity
     * @throws com.jobmanager.orchestrator.domain.exception.JobNotFoundException if job not found
     */
    @Transactional(readOnly = true)
    public JobStatus getJobStatus(UUID uuid) {
        logger.debug("Retrieving job status for UUID: {}", uuid);
        return jobStatusRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    logger.warn("Job not found for UUID: {}", uuid);
                    return new com.jobmanager.orchestrator.domain.exception.JobNotFoundException(
                            "Job not found for UUID: " + uuid);
                });
    }
}

