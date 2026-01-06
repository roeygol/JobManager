package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating or updating a JobRestMapping.
 */
@Schema(description = "Request payload for job-to-service mapping")
public class JobRestMappingRequest {

    @Schema(description = "Job name (part of composite unique key)", example = "data-processing", required = true)
    @NotBlank(message = "jobName is required")
    private String jobName;

    @Schema(description = "Target service name", example = "data-service", required = true)
    @NotBlank(message = "serviceName is required")
    private String serviceName;

    @Schema(description = "Base URL of the service (part of composite unique key)", example = "http://localhost", required = true)
    @NotBlank(message = "url is required")
    private String url;

    @Schema(description = "Service port", example = "8081", required = true)
    @NotNull(message = "port is required")
    private Integer port;

    @Schema(description = "HTTP method (part of composite unique key)", example = "POST", required = true, allowableValues = {"GET", "POST", "PUT", "PATCH", "DELETE"})
    @NotBlank(message = "httpMethod is required")
    private String httpMethod;

    public JobRestMappingRequest() {
    }

    public JobRestMappingRequest(String jobName, String serviceName, String url, Integer port, String httpMethod) {
        this.jobName = jobName;
        this.serviceName = serviceName;
        this.url = url;
        this.port = port;
        this.httpMethod = httpMethod;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
}
