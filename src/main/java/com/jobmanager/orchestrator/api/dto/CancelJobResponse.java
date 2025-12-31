package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * DTO for job cancellation response.
 */
@Schema(description = "Response for job cancellation request")
public class CancelJobResponse {

    @Schema(description = "Cancellation status message", example = "Job cancellation request processed")
    private String message;

    @Schema(description = "Unique identifier for the job execution", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID uuid;

    public CancelJobResponse() {
    }

    public CancelJobResponse(String message, UUID uuid) {
        this.message = message;
        this.uuid = uuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}

