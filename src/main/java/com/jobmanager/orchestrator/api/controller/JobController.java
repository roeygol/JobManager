package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.CancelJobResponse;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for job orchestration operations.
 * Exposes endpoints for job creation, status polling, and cancellation.
 */
@RestController
@RequestMapping("/job")
@Tag(name = "Job Orchestration", description = "API for job execution, status tracking, and cancellation")
@Validated
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final JobOrchestrationService orchestrationService;
    private final JobStatusMapper statusMapper;

    public JobController(JobOrchestrationService orchestrationService, JobStatusMapper statusMapper) {
        this.orchestrationService = orchestrationService;
        this.statusMapper = statusMapper;
    }

    /**
     * Creates and executes a job by forwarding the entire incoming HTTP request.
     * Supports any HTTP method (POST, PUT, DELETE, PATCH, etc.).
     * 
     * @param jobName the job name to resolve destination service
     * @param idempotencyKey the idempotency key header (required)
     * @param request the incoming HTTP request (method, headers, body, query params)
     * @return JobExecutionResponse containing the job UUID
     */
    @RequestMapping(value = "/create/{jobName}", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.GET})
    @Operation(summary = "Create and execute a job", 
               description = "Creates a new job execution by forwarding the entire incoming HTTP request to the target service. " +
                           "Supports any HTTP method, headers, query parameters, and request body. " +
                           "Requires Idempotency-Key header for idempotent requests.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job execution created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or missing Idempotency-Key header"),
        @ApiResponse(responseCode = "404", description = "Job mapping not found")
    })
    public ResponseEntity<JobExecutionResponse> createJob(
            @Parameter(description = "Job name to resolve destination service", required = true)
            @PathVariable 
            @NotBlank(message = "Job name is required")
            @Size(max = 255, message = "Job name must not exceed 255 characters")
            String jobName,
            @Parameter(description = "Idempotency key for the request", required = true)
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key header is required")
            @Size(max = 255, message = "Idempotency-Key must not exceed 255 characters")
            String idempotencyKey,
            HttpServletRequest request) {
        
        logger.info("Received job creation request for job name: {} with method: {}", jobName, request.getMethod());

        try {

            // Extract HTTP method
            HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());

            // Extract all headers (excluding Idempotency-Key as it's internal)
            Map<String, String> headers = extractHeaders(request);

            // Extract query parameters
            Map<String, String> queryParams = extractQueryParams(request);

            // Extract request body
            Object requestBody = extractRequestBody(request);

            // Create and trigger job
            UUID executionUuid = orchestrationService.createAndTriggerJob(
                    jobName, idempotencyKey, httpMethod, headers, queryParams, requestBody);

            logger.info("Job execution created with UUID: {} for job: {}", executionUuid, jobName);
            return ResponseEntity.ok(new JobExecutionResponse(executionUuid));

        } catch (JobOrchestrationService.IdempotentRequestException e) {
            // Idempotent request - return existing UUID
            logger.info("Idempotent request detected for job: {}, returning existing UUID: {}", 
                    jobName, e.getExistingUuid());
            return ResponseEntity.ok(new JobExecutionResponse(e.getExistingUuid()));

        } catch (JobMappingNotFoundException e) {
            logger.warn("Job mapping not found: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid HTTP method: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Returns the current execution status of a job.
     * 
     * @param uuid the job execution UUID
     * @return JobStatusResponse containing status, response payload, HTTP status, timestamps
     */
    @GetMapping("/{uuid}")
    @Operation(summary = "Get job execution status", 
               description = "Retrieves the current status of a job execution by UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job status retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<JobStatusResponse> getJobStatus(
            @Parameter(description = "Job execution UUID", required = true)
            @PathVariable 
            @NotBlank(message = "UUID is required")
            @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
                     message = "Invalid UUID format")
            String uuid) {
        
        UUID jobUuid = UUID.fromString(uuid);
        logger.debug("Received status request for UUID: {}", jobUuid);

        JobStatus jobStatus = orchestrationService.getJobStatus(jobUuid);
        JobStatusResponse response = statusMapper.toDto(jobStatus);

        return ResponseEntity.ok(response);
    }

    /**
     * Cancels an in-flight job execution.
     * 
     * @param uuid the job execution UUID
     * @return ResponseEntity with success status
     */
    @PostMapping("/cancel/{uuid}")
    @Operation(summary = "Cancel a job execution", 
               description = "Cancels an in-flight job execution. " +
                           "If the job is already completed, this is a no-op.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job cancellation request processed"),
        @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<CancelJobResponse> cancelJob(
            @Parameter(description = "Job execution UUID", required = true)
            @PathVariable 
            @NotBlank(message = "UUID is required")
            @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
                     message = "Invalid UUID format")
            String uuid) {
        
        UUID jobUuid = UUID.fromString(uuid);
        logger.info("Received cancellation request for UUID: {}", jobUuid);

        try {
            orchestrationService.cancelJob(jobUuid);
            CancelJobResponse response = new CancelJobResponse(
                    "Job cancellation request processed", 
                    jobUuid);
            return ResponseEntity.ok(response);

        } catch (JobNotFoundException e) {
            logger.warn("Job not found for cancellation: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts all headers from the request, excluding internal headers.
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Exclude Idempotency-Key as it's internal
            if (!IDEMPOTENCY_KEY_HEADER.equalsIgnoreCase(headerName)) {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        
        return headers;
    }

    /**
     * Extracts query parameters from the request.
     */
    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        Map<String, String> queryParams = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            queryParams.put(paramName, request.getParameter(paramName));
        }
        
        return queryParams;
    }

    /**
     * Extracts request body from the request.
     * Returns null if no body is present.
     */
    private Object extractRequestBody(HttpServletRequest request) {
        try {
            String contentType = request.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                // For non-JSON content types, return null or handle differently
                return null;
            }

            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            if (body == null || body.isBlank()) {
                return null;
            }

            // Return as String for now - HttpForwardingService will handle it
            return body;

        } catch (IOException e) {
            logger.warn("Failed to read request body: {}", e.getMessage());
            return null;
        }
    }
}
