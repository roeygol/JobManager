package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * DTO for job execution response containing the execution UUID.
 */
@Schema(description = "Response containing the UUID of the created job execution")
public class JobExecutionResponse {

    @Schema(description = "Unique identifier for the job execution", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID uuid;

    public JobExecutionResponse() {
    }

    public JobExecutionResponse(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}

