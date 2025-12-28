package com.jobmanager.orchestrator.api.dto;

import java.util.UUID;

/**
 * DTO for job execution response containing the execution UUID.
 */
public class JobExecutionResponse {

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

