package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.JobRestMappingRequest;
import com.jobmanager.orchestrator.api.dto.JobRestMappingResponse;
import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.persistence.repository.JobRestMappingRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for CRUD operations on JobRestMapping (JPA).
 */
@RestController
@RequestMapping("/job-mappings")
@Tag(name = "Job Rest Mappings", description = "CRUD API for job-to-service mappings")
@Validated
public class JobRestMappingController {

    private static final Logger logger = LoggerFactory.getLogger(JobRestMappingController.class);

    private final JobRestMappingRepository jobRestMappingRepository;

    public JobRestMappingController(JobRestMappingRepository jobRestMappingRepository) {
        this.jobRestMappingRepository = jobRestMappingRepository;
    }

    /**
     * Create a new mapping.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create mapping", description = "Creates a new job-to-service mapping")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mapping created"),
            @ApiResponse(responseCode = "409", description = "Mapping with jobName already exists")
    })
    public ResponseEntity<JobRestMappingResponse> createMapping(
            @Valid @RequestBody JobRestMappingRequest request) {
        logger.info("Creating job mapping for jobName={}", request.getJobName());

        jobRestMappingRepository.findByJobName(request.getJobName()).ifPresent(existing -> {
            throw new DataIntegrityViolationException("Mapping already exists for jobName=" + request.getJobName());
        });

        JobRestMapping saved = jobRestMappingRepository.save(toEntity(new JobRestMapping(), request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    /**
     * Get mapping by ID.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get mapping by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping found"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<JobRestMappingResponse> getById(
            @Parameter(description = "Mapping ID", required = true)
            @PathVariable Long id) {
        Optional<JobRestMapping> mapping = jobRestMappingRepository.findById(id);
        return mapping.map(value -> ResponseEntity.ok(toResponse(value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Get mapping by job name.
     */
    @GetMapping(value = "/by-name/{jobName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get mapping by job name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping found"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<JobRestMappingResponse> getByJobName(
            @Parameter(description = "Job name", required = true)
            @PathVariable @NotBlank String jobName) {
        Optional<JobRestMapping> mapping = jobRestMappingRepository.findByJobName(jobName);
        return mapping.map(value -> ResponseEntity.ok(toResponse(value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * List all mappings.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List mappings")
    public ResponseEntity<List<JobRestMappingResponse>> listAll() {
        List<JobRestMappingResponse> responses = jobRestMappingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Update mapping by ID.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update mapping by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping updated"),
            @ApiResponse(responseCode = "404", description = "Mapping not found"),
            @ApiResponse(responseCode = "409", description = "Job name conflict")
    })
    public ResponseEntity<JobRestMappingResponse> updateMapping(
            @Parameter(description = "Mapping ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody JobRestMappingRequest request) {
        Optional<JobRestMapping> existing = jobRestMappingRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Ensure unique jobName when updating
        jobRestMappingRepository.findByJobName(request.getJobName())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(conflict -> {
                    throw new DataIntegrityViolationException("Mapping already exists for jobName=" + request.getJobName());
                });

        JobRestMapping toSave = toEntity(existing.get(), request);
        JobRestMapping saved = jobRestMappingRepository.save(toSave);
        return ResponseEntity.ok(toResponse(saved));
    }

    /**
     * Delete mapping by ID.
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete mapping by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping deleted"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<Void> deleteMapping(
            @Parameter(description = "Mapping ID", required = true)
            @PathVariable Long id) {
        if (!jobRestMappingRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        jobRestMappingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private JobRestMapping toEntity(JobRestMapping entity, JobRestMappingRequest request) {
        entity.setJobName(request.getJobName());
        entity.setServiceName(request.getServiceName());
        entity.setUrl(request.getUrl());
        entity.setPort(request.getPort());
        return entity;
    }

    private JobRestMappingResponse toResponse(JobRestMapping entity) {
        return new JobRestMappingResponse(
                entity.getId(),
                entity.getJobName(),
                entity.getServiceName(),
                entity.getUrl(),
                entity.getPort()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConflict(DataIntegrityViolationException ex) {
        logger.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.JobRestMappingRequest;
import com.jobmanager.orchestrator.api.dto.JobRestMappingResponse;
import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.persistence.repository.JobRestMappingRepository;
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
 * REST controller providing CRUD endpoints for JobRestMapping.
 */
@RestController
@RequestMapping("/job-rest-mappings")
@Tag(name = "Job REST Mappings", description = "CRUD API for job-to-service mappings")
@Validated
public class JobRestMappingController {

    private static final Logger logger = LoggerFactory.getLogger(JobRestMappingController.class);

    @Autowired
    private JobRestMappingRepository repository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create job mapping", description = "Creates a new job-to-service mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mapping created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Job name already exists")
    })
    public ResponseEntity<JobRestMappingResponse> create(@Valid @RequestBody JobRestMappingRequest request) {
        logger.info("Creating job mapping for jobName={}", request.getJobName());

        if (repository.findByJobName(request.getJobName()).isPresent()) {
            logger.warn("Job mapping already exists for jobName={}", request.getJobName());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        JobRestMapping saved = repository.save(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get mapping by ID", description = "Retrieves a mapping by database ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping found"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<JobRestMappingResponse> getById(
            @Parameter(description = "Mapping ID", required = true)
            @PathVariable Long id) {
        Optional<JobRestMapping> mapping = repository.findById(id);
        return mapping.map(value -> ResponseEntity.ok(toResponse(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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

        Optional<JobRestMapping> mapping = repository.findByJobName(jobName);
        return mapping.map(value -> ResponseEntity.ok(toResponse(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all mappings", description = "Returns all job-to-service mappings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mappings retrieved")
    })
    public ResponseEntity<List<JobRestMappingResponse>> listAll() {
        List<JobRestMappingResponse> mappings = repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mappings);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update mapping", description = "Updates an existing mapping by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping updated"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<JobRestMappingResponse> update(
            @Parameter(description = "Mapping ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody JobRestMappingRequest request) {

        Optional<JobRestMapping> existing = repository.findById(id);
        if (existing.isEmpty()) {
            logger.warn("Mapping not found for id={}", id);
            return ResponseEntity.notFound().build();
        }

        JobRestMapping mapping = existing.get();
        mapping.setJobName(request.getJobName());
        mapping.setServiceName(request.getServiceName());
        mapping.setUrl(request.getUrl());
        mapping.setPort(request.getPort());

        JobRestMapping saved = repository.save(mapping);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete mapping by ID", description = "Deletes a mapping by database ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping deleted"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<Map<String, Object>> deleteById(
            @Parameter(description = "Mapping ID", required = true)
            @PathVariable Long id) {

        if (repository.existsById(id)) {
            repository.deleteById(id);
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

        Optional<JobRestMapping> mapping = repository.findByJobName(jobName);
        if (mapping.isPresent()) {
            repository.delete(mapping.get());
            Map<String, Object> response = Map.of("jobName", jobName, "deleted", true);
            return ResponseEntity.ok(response);
        }
        Map<String, Object> response = Map.of("jobName", jobName, "deleted", false, "message", "Mapping not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    private JobRestMapping toEntity(JobRestMappingRequest request) {
        return new JobRestMapping(request.getJobName(), request.getServiceName(), request.getUrl(), request.getPort());
    }

    private JobRestMappingResponse toResponse(JobRestMapping mapping) {
        return new JobRestMappingResponse(
                mapping.getId(),
                mapping.getJobName(),
                mapping.getServiceName(),
                mapping.getUrl(),
                mapping.getPort()
        );
    }
}

