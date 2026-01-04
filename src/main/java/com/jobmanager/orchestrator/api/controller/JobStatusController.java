package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.DeleteJobStatusResponse;
import com.jobmanager.orchestrator.api.dto.JobStatusDocumentResponse;
import com.jobmanager.orchestrator.api.dto.SaveJobStatusRequest;
import com.jobmanager.orchestrator.application.service.MongoDbService;
import com.jobmanager.orchestrator.persistence.mongodb.document.JobStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for MongoDB job status operations.
 * Provides endpoints to store, retrieve, and manage job status information in MongoDB.
 */
@RestController
@RequestMapping("/mongo/job-statuses")
@Tag(name = "MongoDB Job Statuses", description = "API for managing job status information in MongoDB")
@Validated
public class JobStatusController {

    private static final Logger logger = LoggerFactory.getLogger(JobStatusController.class);

    @Autowired
    private MongoDbService mongoDbService;

    /**
     * Saves or updates a job status in MongoDB.
     *
     * @param request the job status save request
     * @return the saved job status
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Save or update a job status", 
               description = "Saves a new job status or updates an existing one by UUID. " +
                           "If a job status with the same UUID exists, it will be updated.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job status saved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<JobStatusDocumentResponse> saveJobStatus(@Valid @RequestBody SaveJobStatusRequest request) {
        logger.info("Received save job status request for UUID: {}", request.getUuid());

        JobStatus saved = mongoDbService.saveJobStatus(
                request.getUuid(),
                request.getStatus(),
                request.getResponse(),
                request.getStartDate(),
                request.getEndDate(),
                request.getHttpCode()
        );
        JobStatusDocumentResponse response = toJobStatusResponse(saved);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a job status by its UUID.
     *
     * @param uuid the UUID
     * @return the job status if found
     */
    @GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get job status by UUID", 
               description = "Retrieves a job status from MongoDB by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job status not found")
    })
    public ResponseEntity<JobStatusDocumentResponse> getJobStatusByUuid(
            @Parameter(description = "Job UUID", required = true)
            @PathVariable @NotBlank(message = "UUID is required") String uuid) {
        
        logger.debug("Retrieving job status with UUID: {}", uuid);

        Optional<JobStatus> jobStatus = mongoDbService.getByUuid(uuid);
        
        if (jobStatus.isPresent()) {
            return ResponseEntity.ok(toJobStatusResponse(jobStatus.get()));
        } else {
            logger.warn("Job status not found with UUID: {}", uuid);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves a job status as JSON string.
     *
     * @param uuid the UUID
     * @return the job status as JSON string
     */
    @GetMapping(value = "/{uuid}/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get job status as JSON string", 
               description = "Retrieves a job status from MongoDB as JSON string")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job status not found")
    })
    public ResponseEntity<Map<String, String>> getJobStatusAsJson(
            @Parameter(description = "Job UUID", required = true)
            @PathVariable @NotBlank(message = "UUID is required") String uuid) {
        
        logger.debug("Retrieving job status as JSON for UUID: {}", uuid);

        Optional<String> jsonString = mongoDbService.getJobStatusAsJsonString(uuid);
        
        if (jsonString.isPresent()) {
            Map<String, String> response = Map.of("uuid", uuid, "json", jsonString.get());
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Job status not found with UUID: {}", uuid);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves all job statuses from MongoDB.
     *
     * @return list of all job statuses
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all job statuses", 
               description = "Retrieves all job statuses from MongoDB")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job statuses retrieved successfully")
    })
    public ResponseEntity<List<JobStatusDocumentResponse>> getAllJobStatuses() {
        logger.debug("Retrieving all job statuses");

        List<JobStatusDocumentResponse> jobStatuses = mongoDbService.getAllJobStatuses().stream()
                .map(this::toJobStatusResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(jobStatuses);
    }

    /**
     * Checks if a job status exists by its UUID.
     *
     * @param uuid the UUID
     * @return true if job status exists, false otherwise
     */
    @GetMapping(value = "/{uuid}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check if job status exists", 
               description = "Checks if a job status exists in MongoDB by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Existence check completed")
    })
    public ResponseEntity<Map<String, Object>> jobStatusExists(
            @Parameter(description = "Job UUID", required = true)
            @PathVariable @NotBlank(message = "UUID is required") String uuid) {
        
        logger.debug("Checking existence of job status with UUID: {}", uuid);

        boolean exists = mongoDbService.jobStatusExists(uuid);
        Map<String, Object> response = Map.of(
                "uuid", uuid,
                "exists", exists
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a job status by its UUID.
     *
     * @param uuid the UUID
     * @return deletion result
     */
    @DeleteMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete job status by UUID", 
               description = "Deletes a job status from MongoDB by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job status deletion processed"),
        @ApiResponse(responseCode = "404", description = "Job status not found")
    })
    public ResponseEntity<DeleteJobStatusResponse> deleteJobStatus(
            @Parameter(description = "Job UUID", required = true)
            @PathVariable @NotBlank(message = "UUID is required") String uuid) {
        
        logger.info("Deleting job status with UUID: {}", uuid);

        boolean deleted = mongoDbService.deleteJobStatus(uuid);
        
        if (deleted) {
            DeleteJobStatusResponse response = new DeleteJobStatusResponse(
                    true, "Job status deleted successfully", uuid);
            return ResponseEntity.ok(response);
        } else {
            DeleteJobStatusResponse response = new DeleteJobStatusResponse(
                    false, "Job status not found", uuid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Converts JobStatus entity to JobStatusDocumentResponse DTO.
     */
    private JobStatusDocumentResponse toJobStatusResponse(JobStatus jobStatus) {
        return new JobStatusDocumentResponse(
                jobStatus.getId(),
                jobStatus.getUuid(),
                jobStatus.getStatus(),
                jobStatus.getResponse(),
                jobStatus.getStartDate(),
                jobStatus.getEndDate(),
                jobStatus.getHttpCode(),
                jobStatus.getCreatedAt(),
                jobStatus.getUpdatedAt()
        );
    }
}

