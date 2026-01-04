package com.jobmanager.orchestrator.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO for MongoDB job status response.
 */
@Schema(description = "Response containing MongoDB job status information")
public class JobStatusDocumentResponse {

    @Schema(description = "Document ID", example = "507f1f77bcf86cd799439011")
    @JsonProperty("id")
    private String id;

    @Schema(description = "Job UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("uuid")
    private String uuid;

    @Schema(description = "Job execution status", example = "COMPLETED")
    @JsonProperty("status")
    private JobExecutionStatus status;

    @Schema(description = "Response message", example = "Job completed successfully")
    @JsonProperty("response")
    private String response;

    @Schema(description = "Start date", example = "2024-01-01T10:00:00")
    @JsonProperty("startDate")
    private LocalDateTime startDate;

    @Schema(description = "End date", example = "2024-01-01T10:05:00")
    @JsonProperty("endDate")
    private LocalDateTime endDate;

    @Schema(description = "HTTP status code", example = "200")
    @JsonProperty("httpCode")
    private Integer httpCode;

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    public JobStatusDocumentResponse() {
    }

    public JobStatusDocumentResponse(String id, String uuid, JobExecutionStatus status, String response,
                                     LocalDateTime startDate, LocalDateTime endDate, Integer httpCode,
                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.uuid = uuid;
        this.status = status;
        this.response = response;
        this.startDate = startDate;
        this.endDate = endDate;
        this.httpCode = httpCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public JobExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(JobExecutionStatus status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

