package com.jobmanager.orchestrator.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO for saving a job status to MongoDB.
 */
@Schema(description = "Request to save or update a job status in MongoDB")
public class SaveJobStatusRequest {

    @Schema(description = "Job UUID (unique identifier)", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @NotBlank(message = "UUID is required")
    @JsonProperty("uuid")
    private String uuid;

    @Schema(description = "Job execution status", example = "RUNNING", required = true)
    @NotNull(message = "Status is required")
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

    public SaveJobStatusRequest() {
    }

    public SaveJobStatusRequest(String uuid, JobExecutionStatus status, String response,
                                LocalDateTime startDate, LocalDateTime endDate, Integer httpCode) {
        this.uuid = uuid;
        this.status = status;
        this.response = response;
        this.startDate = startDate;
        this.endDate = endDate;
        this.httpCode = httpCode;
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
}

