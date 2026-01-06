package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for Job REST mapping responses.
 */
@Schema(description = "Response containing Job REST mapping information")
public class JobRestMappingResponse {

    @Schema(description = "MongoDB identifier", example = "665b8f8f2a5f4b3c9c8a1234")
    private String id;

    @Schema(description = "Job name (unique)", example = "data-processing")
    private String jobName;

    @Schema(description = "Service name that handles the job", example = "data-service")
    private String serviceName;

    @Schema(description = "Base URL of the service", example = "http://localhost")
    private String url;

    @Schema(description = "Service port", example = "8081")
    private Integer port;

    public JobRestMappingResponse() {
    }

    public JobRestMappingResponse(String id, String jobName, String serviceName, String url, Integer port) {
        this.id = id;
        this.jobName = jobName;
        this.serviceName = serviceName;
        this.url = url;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}

