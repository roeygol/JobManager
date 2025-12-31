package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for create job request.
 * Contains validated fields for job creation.
 */
@Schema(description = "Request to create a job execution")
public class CreateJobRequest {

    @NotBlank(message = "Job name is required")
    @Size(max = 255, message = "Job name must not exceed 255 characters")
    @Schema(description = "Name of the job to execute", example = "data-processing", required = true)
    private String jobName;

    @NotBlank(message = "Idempotency-Key header is required")
    @Size(max = 255, message = "Idempotency-Key must not exceed 255 characters")
    @Schema(description = "Idempotency key for the request", example = "unique-request-id-123", required = true)
    private String idempotencyKey;

    public CreateJobRequest() {
    }

    public CreateJobRequest(String jobName, String idempotencyKey) {
        this.jobName = jobName;
        this.idempotencyKey = idempotencyKey;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}

