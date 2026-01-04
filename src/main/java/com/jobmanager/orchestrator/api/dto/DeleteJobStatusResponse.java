package com.jobmanager.orchestrator.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for job status deletion response.
 */
@Schema(description = "Response for job status deletion operation")
public class DeleteJobStatusResponse {

    @Schema(description = "Indicates if the job status was deleted", example = "true")
    @JsonProperty("deleted")
    private boolean deleted;

    @Schema(description = "Message describing the result", example = "Job status deleted successfully")
    @JsonProperty("message")
    private String message;

    @Schema(description = "UUID that was deleted", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("uuid")
    private String uuid;

    public DeleteJobStatusResponse() {
    }

    public DeleteJobStatusResponse(boolean deleted, String message, String uuid) {
        this.deleted = deleted;
        this.message = message;
        this.uuid = uuid;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

