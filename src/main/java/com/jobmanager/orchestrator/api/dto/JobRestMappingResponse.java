package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing a JobRestMapping response.
 */
@Schema(description = "Job-to-service mapping response")
public class JobRestMappingResponse {

    @Schema(description = "Record ID", example = "1")
    private Long id;

    @Schema(description = "Job name (unique key)", example = "data-processing")
    private String jobName;

    @Schema(description = "Target service name", example = "data-service")
    private String serviceName;

    @Schema(description = "Base URL of the service", example = "http://localhost")
    private String url;

    @Schema(description = "Service port", example = "8081")
    private Integer port;

    public JobRestMappingResponse() {
    }

    public JobRestMappingResponse(Long id, String jobName, String serviceName, String url, Integer port) {
        this.id = id;
        this.jobName = jobName;
        this.serviceName = serviceName;
        this.url = url;
        this.port = port;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for Job REST mapping responses.
 */
@Schema(description = "Response containing Job REST mapping information")
public class JobRestMappingResponse {

    @Schema(description = "Database identifier", example = "1")
    private Long id;

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

    public JobRestMappingResponse(Long id, String jobName, String serviceName, String url, Integer port) {
        this.id = id;
        this.jobName = jobName;
        this.serviceName = serviceName;
        this.url = url;
        this.port = port;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

