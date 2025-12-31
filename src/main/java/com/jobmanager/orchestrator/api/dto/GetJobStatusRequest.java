package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for get job status request.
 * Used for validation of path variable.
 */
@Schema(description = "Request to get job execution status")
public class GetJobStatusRequest {

    @NotBlank(message = "UUID is required")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
             message = "Invalid UUID format")
    @Schema(description = "Job execution UUID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String uuid;

    public GetJobStatusRequest() {
    }

    public GetJobStatusRequest(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

