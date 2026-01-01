package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.domain.entity.JobStatus;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import com.jobmanager.orchestrator.domain.exception.JobNotFoundException;
import com.jobmanager.orchestrator.persistence.repository.JobStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Core orchestration service responsible for job execution lifecycle management.
 * Handles idempotency, async execution, cancellation, and state persistence.
 */
@Service
public class JobOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(JobOrchestrationService.class);

    @Autowired
    private JobMappingService mappingService;
    
    @Autowired
    private HttpForwardingService httpForwardingService;
    
    @Autowired
    private JobStatusRepository jobStatusRepository;

    // Thread-safe registry of active job executions: UUID -> Future
    private final Map<UUID, Future<?>> executionRegistry = new ConcurrentHashMap<>();

    /**
     * Creates and triggers a job execution asynchronously with idempotency support.
     *
     * @param jobName the job name to execute
     * @param idempotencyKey the idempotency key (required)
     * @param httpMethod the HTTP method to forward
     * @param headers the request headers to forward
     * @param queryParams the query parameters to forward
     * @param requestBody the request body to forward
     * @return the UUID of the created or existing job execution
     */
    @Transactional
    public UUID createAndTriggerJob(
            String jobName,
            String idempotencyKey,
            HttpMethod httpMethod,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Object requestBody) {
        
        logger.info("Creating job execution for job name: {} with idempotency key: {}", jobName, idempotencyKey);

        // Check idempotency: same jobName + idempotencyKey should return existing UUID
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            jobStatusRepository.findByIdempotencyKey(idempotencyKey)
                    .ifPresent(existingJob -> {
                        logger.info("Idempotent request detected. Returning existing job UUID: {} for key: {}", 
                                existingJob.getUuid(), idempotencyKey);
                        throw new IdempotentRequestException(existingJob.getUuid());
                    });
        }

        // Resolve mapping
        JobRestMapping mapping = mappingService.resolveMapping(jobName);
        logger.debug("Resolved mapping: {} -> {}:{}", jobName, mapping.getServiceName(), mapping.getFullEndpointUrl());

        // Create job status entity
        UUID executionUuid = UUID.randomUUID();
        JobStatus jobStatus = new JobStatus(executionUuid, JobExecutionStatus.STARTED);
        jobStatus.setIdempotencyKey(idempotencyKey);
        jobStatus = jobStatusRepository.save(jobStatus);
        logger.info("Created job status with UUID: {}", executionUuid);

        // Trigger asynchronous execution and register Future
        CompletableFuture<Void> future = executeJobAsync(jobStatus, mapping, httpMethod, headers, queryParams, requestBody);
        executionRegistry.put(executionUuid, future);

        // Clean up registry when future completes
        future.whenComplete((result, throwable) -> {
            executionRegistry.remove(executionUuid);
            logger.debug("Removed job UUID {} from execution registry", executionUuid);
        });

        return executionUuid;
    }

    /**
     * Executes the job asynchronously and updates status.
     * Note: Entity must be reloaded in async context to avoid detached entity issues.
     */
    @Async("jobExecutor")
    @Transactional
    public CompletableFuture<Void> executeJobAsync(
            JobStatus jobStatus,
            JobRestMapping mapping,
            HttpMethod httpMethod,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Object requestBody) {
        
        UUID uuid = jobStatus.getUuid();
        logger.info("Starting asynchronous execution for job UUID: {}", uuid);

        try {
            // Reload entity in async context to ensure it's managed
            JobStatus managedStatus = jobStatusRepository.findByUuid(uuid)
                    .orElseThrow(() -> new IllegalStateException("Job status not found for UUID: " + uuid));

            // Check if already cancelled
            if (managedStatus.getStatus() == JobExecutionStatus.CANCELLED) {
                logger.info("Job UUID {} was cancelled before execution started", uuid);
                return CompletableFuture.completedFuture(null);
            }

            // Update status to IN_PROGRESS
            managedStatus.setStatus(JobExecutionStatus.IN_PROGRESS);
            managedStatus = jobStatusRepository.save(managedStatus);
            logger.debug("Updated job status to IN_PROGRESS for UUID: {}", uuid);

            // Forward HTTP request
            String endpointUrl = mapping.getFullEndpointUrl();
            logger.info("Forwarding {} request to endpoint: {} for job: {}", 
                    httpMethod, endpointUrl, mapping.getJobName());
            
            HttpForwardingService.HttpForwardingResponse response = httpForwardingService.forwardRequest(
                    httpMethod, endpointUrl, headers, queryParams, requestBody);

            // Check if cancelled during execution
            managedStatus = jobStatusRepository.findByUuid(uuid)
                    .orElseThrow(() -> new IllegalStateException("Job status not found for UUID: " + uuid));
            
            if (managedStatus.getStatus() == JobExecutionStatus.CANCELLED) {
                logger.info("Job UUID {} was cancelled during execution", uuid);
                return CompletableFuture.completedFuture(null);
            }

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
                // Only update if not already cancelled
                if (status.getStatus() != JobExecutionStatus.CANCELLED) {
                    status.setStatus(JobExecutionStatus.FAILED);
                    status.setResponse("Execution error: " + e.getMessage());
                    status.setHttpStatus(0);
                    status.setEndDate(LocalDateTime.now());
                    jobStatusRepository.save(status);
                }
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Retrieves job status by UUID.
     *
     * @param uuid the job execution UUID
     * @return the JobStatus entity
     * @throws JobNotFoundException if job not found
     */
    @Transactional(readOnly = true)
    public JobStatus getJobStatus(UUID uuid) {
        logger.debug("Retrieving job status for UUID: {}", uuid);
        return jobStatusRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    logger.warn("Job not found for UUID: {}", uuid);
                    return new JobNotFoundException("Job not found for UUID: " + uuid);
                });
    }

    /**
     * Cancels an in-flight job execution.
     *
     * @param uuid the job execution UUID
     * @throws JobNotFoundException if job not found
     */
    @Transactional
    public void cancelJob(UUID uuid) {
        logger.info("Cancelling job execution for UUID: {}", uuid);

        JobStatus jobStatus = jobStatusRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    logger.warn("Job not found for UUID: {}", uuid);
                    return new JobNotFoundException("Job not found for UUID: " + uuid);
                });

        // Check if already completed
        if (jobStatus.getStatus() == JobExecutionStatus.SUCCESS || 
            jobStatus.getStatus() == JobExecutionStatus.FAILED ||
            jobStatus.getStatus() == JobExecutionStatus.CANCELLED) {
            logger.info("Job UUID {} is already in terminal state: {}", uuid, jobStatus.getStatus());
            return; // No-op for completed jobs
        }

        // Cancel the executing thread
        Future<?> future = executionRegistry.get(uuid);
        if (future != null) {
            boolean cancelled = future.cancel(true); // Interrupt if running
            logger.info("Cancelled future for job UUID: {}, cancelled: {}", uuid, cancelled);
        }

        // Update status to CANCELLED
        jobStatus.setStatus(JobExecutionStatus.CANCELLED);
        jobStatus.setEndDate(LocalDateTime.now());
        jobStatus.setResponse("Job execution was cancelled");
        jobStatusRepository.save(jobStatus);

        // Remove from registry
        executionRegistry.remove(uuid);

        logger.info("Job UUID {} cancelled successfully", uuid);
    }

    /**
     * Exception thrown when an idempotent request is detected.
     * Contains the existing job UUID.
     */
    public static class IdempotentRequestException extends RuntimeException {
        private final UUID existingUuid;

        public IdempotentRequestException(UUID existingUuid) {
            super("Idempotent request detected. Existing job UUID: " + existingUuid);
            this.existingUuid = existingUuid;
        }

        public UUID getExistingUuid() {
            return existingUuid;
        }
    }
}
