package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for job execution request.
 */
@Schema(description = "Request to create and trigger a job execution")
public class JobExecutionRequest {

    @NotBlank(message = "Job name is required")
    @Size(max = 255, message = "Job name must not exceed 255 characters")
    @Schema(description = "Name of the job to execute", example = "data-processing", required = true)
    private String jobName;

    public JobExecutionRequest() {
    }

    public JobExecutionRequest(String jobName) {
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}

