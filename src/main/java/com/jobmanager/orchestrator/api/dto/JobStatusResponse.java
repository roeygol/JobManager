package com.jobmanager.orchestrator.api.dto;

import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for job status response containing full execution state.
 */
@Schema(description = "Response containing the current status and details of a job execution")
public class JobStatusResponse {

    @Schema(description = "Unique identifier for the job execution", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID uuid;
    
    @Schema(description = "Current status of the job execution", example = "SUCCESS")
    private JobExecutionStatus status;
    
    @Schema(description = "Response body from the remote job execution", example = "Job completed successfully")
    private String response;
    
    @Schema(description = "HTTP status code from the remote job execution", example = "200")
    private Integer httpStatus;
    
    @Schema(description = "Timestamp when the job execution started", example = "2024-01-01T10:00:00")
    private LocalDateTime startDate;
    
    @Schema(description = "Timestamp when the job execution ended", example = "2024-01-01T10:00:15")
    private LocalDateTime endDate;

    public JobStatusResponse() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
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
}

