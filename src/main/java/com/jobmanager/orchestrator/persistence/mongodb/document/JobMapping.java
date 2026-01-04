package com.jobmanager.orchestrator.persistence.mongodb.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document entity for storing job mapping configurations.
 * Maps job names to downstream service endpoints for job execution routing.
 * This is the MongoDB equivalent of the JPA JobRestMapping entity.
 * 
 * Structure:
 * - Key: jobName (unique identifier)
 * - Fields: serviceName, port, url, and other configuration fields
 */
@Document(collection = "job_mappings")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobMapping extends BaseMongoDocument {

    @Indexed(unique = true)
    @JsonProperty("jobName")
    private String jobName;

    @JsonProperty("serviceName")
    private String serviceName;
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("port")
    private Integer port;

    public JobMapping() {
        super();
    }

    public JobMapping(String jobName, String serviceName, String url, Integer port) {
        super();
        this.jobName = jobName;
        this.serviceName = serviceName;
        this.url = url;
        this.port = port;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
        touch();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
        touch();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        touch();
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
        touch();
    }

    /**
     * Constructs the full endpoint URL for this mapping.
     *
     * @return the full endpoint URL in the format "url:port"
     */
    public String getFullEndpointUrl() {
        if (url == null || port == null) {
            return null;
        }
        String baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return String.format("%s:%d", baseUrl, port);
    }
}

