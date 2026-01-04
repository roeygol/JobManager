package com.jobmanager.orchestrator.persistence.mongodb.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document entity for storing job status information.
 * Tracks job execution status, response, and timing information.
 * 
 * Structure:
 * - Key: uuid (unique identifier)
 * - Fields: status, response, startDate, endDate, httpCode
 */
@Document(collection = "job_statuses")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobStatus extends BaseMongoDocument {

    @Indexed(unique = true)
    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("status")
    private JobExecutionStatus status;

    @JsonProperty("response")
    private String response;

    @JsonProperty("startDate")
    private LocalDateTime startDate;

    @JsonProperty("endDate")
    private LocalDateTime endDate;

    @JsonProperty("httpCode")
    private Integer httpCode;

    public JobStatus() {
        super();
    }

    public JobStatus(String uuid, JobExecutionStatus status, String response, 
                     LocalDateTime startDate, LocalDateTime endDate, Integer httpCode) {
        super();
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
        touch();
    }

    public JobExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(JobExecutionStatus status) {
        this.status = status;
        touch();
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
        touch();
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
        touch();
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        touch();
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
        touch();
    }
}

