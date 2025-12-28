package com.jobmanager.orchestrator.api.dto;

import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for job status response containing full execution state.
 */
public class JobStatusResponse {

    private UUID uuid;
    private JobExecutionStatus status;
    private String response;
    private Integer httpStatus;
    private LocalDateTime startDate;
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

