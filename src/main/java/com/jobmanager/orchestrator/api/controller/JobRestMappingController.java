package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.JobRestMappingRequest;
import com.jobmanager.orchestrator.api.dto.JobRestMappingResponse;
import com.jobmanager.orchestrator.application.service.JobMappingMongoService;
import com.jobmanager.orchestrator.persistence.mongodb.document.JobMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * REST controller providing CRUD endpoints for job mappings.
 * Delegates business logic to {@link JobMappingMongoService} which uses MongoDB.
 */
@RestController
@RequestMapping("/job-mappings")
@Tag(name = "Job Rest Mappings", description = "CRUD API for job-to-service mappings")
@Validated
public class JobRestMappingController {

    private static final Logger logger = LoggerFactory.getLogger(JobRestMappingController.class);

    private final JobMappingMongoService service;

    public JobRestMappingController(JobMappingMongoService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create job mapping", description = "Creates a new job-to-service mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mapping created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Job name already exists")
    })
    public ResponseEntity<JobRestMappingResponse> create(@Valid @RequestBody JobRestMappingRequest request) {
        try {
            JobMapping saved = service.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (DataIntegrityViolationException ex) {
            logger.warn("Failed to create mapping: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get mapping by ID", description = "Retrieves a mapping by database ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping found"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<JobRestMappingResponse> getById(
            @Parameter(description = "Mapping ID", required = true)
            @PathVariable String id) {
        return service.getById(id)
                .map(mapping -> ResponseEntity.ok(toResponse(mapping)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(value = "/by-name/{jobName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get mapping by job name", description = "Retrieves a mapping by job name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping found"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<JobRestMappingResponse> getByJobName(
            @Parameter(description = "Job name", required = true)
            @PathVariable @NotBlank(message = "jobName is required") String jobName) {

        return service.getByJobName(jobName)
                .map(mapping -> ResponseEntity.ok(toResponse(mapping)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all mappings", description = "Returns all job-to-service mappings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mappings retrieved")
    })
    public ResponseEntity<List<JobRestMappingResponse>> listAll() {
        List<JobRestMappingResponse> mappings = service.listAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mappings);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update mapping", description = "Updates an existing mapping by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping updated"),
            @ApiResponse(responseCode = "404", description = "Mapping not found"),
            @ApiResponse(responseCode = "409", description = "Job name conflict")
    })
    public ResponseEntity<JobRestMappingResponse> update(
            @Parameter(description = "Mapping ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody JobRestMappingRequest request) {

        try {
            JobMapping saved = service.update(id, request);
            return ResponseEntity.ok(toResponse(saved));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataIntegrityViolationException ex) {
            logger.warn("Failed to update mapping: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete mapping by ID", description = "Deletes a mapping by database ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping deleted"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<Map<String, Object>> deleteById(
            @Parameter(description = "Mapping ID", required = true)
            @PathVariable String id) {

        boolean deleted = service.deleteById(id);
        if (deleted) {
            Map<String, Object> response = Map.of("id", id, "deleted", true);
            return ResponseEntity.ok(response);
        }
        Map<String, Object> response = Map.of("id", id, "deleted", false, "message", "Mapping not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @DeleteMapping(value = "/by-name/{jobName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete mapping by job name", description = "Deletes a mapping by job name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping deleted"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<Map<String, Object>> deleteByJobName(
            @Parameter(description = "Job name", required = true)
            @PathVariable @NotBlank(message = "jobName is required") String jobName) {

        boolean deleted = service.deleteByJobName(jobName);
        if (deleted) {
            Map<String, Object> response = Map.of("jobName", jobName, "deleted", true);
            return ResponseEntity.ok(response);
        }
        Map<String, Object> response = Map.of("jobName", jobName, "deleted", false, "message", "Mapping not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    private JobRestMappingResponse toResponse(JobMapping mapping) {
        return new JobRestMappingResponse(
                mapping.getId(),
                mapping.getJobName(),
                mapping.getServiceName(),
                mapping.getUrl(),
                mapping.getPort()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConflict(DataIntegrityViolationException ex) {
        logger.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}


