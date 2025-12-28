package com.jobmanager.orchestrator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entity representing the mapping configuration between job names and downstream services.
 * Defines routing rules for job execution.
 */
@Entity
@Table(name = "job_rest_mapping", uniqueConstraints = {
    @UniqueConstraint(columnNames = "job_name")
})
public class JobRestMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, unique = true)
    private String jobName;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "port", nullable = false)
    private Integer port;

    public JobRestMapping() {
    }

    public JobRestMapping(String jobName, String serviceName, String url, Integer port) {
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

    /**
     * Constructs the full endpoint URL for this mapping.
     */
    public String getFullEndpointUrl() {
        String baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return String.format("%s:%d", baseUrl, port);
    }
}

