package com.jobmanager.orchestrator.domain.entity;

import com.jobmanager.orchestrator.persistence.mongodb.document.BaseMongoDocument;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing the mapping configuration between job names and downstream services.
 * Defines routing rules for job execution.
 * 
 * Uses a composite unique key: jobName + url + httpMethod
 */
@Document(collection = "job_rest_mapping")
@CompoundIndex(name = "jobName_url_httpMethod_idx", def = "{'jobName': 1, 'url': 1, 'httpMethod': 1}", unique = true)
public class JobRestMapping extends BaseMongoDocument {

    private String jobName;

    private String serviceName;

    private String url;

    private Integer port;

    private String httpMethod;

    public JobRestMapping() {
        super();
    }

    public JobRestMapping(String jobName, String serviceName, String url, Integer port, String httpMethod) {
        super();
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

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
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

