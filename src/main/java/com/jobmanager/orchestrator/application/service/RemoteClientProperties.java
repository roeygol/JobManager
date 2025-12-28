package com.jobmanager.orchestrator.application.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for remote client.
 */
@Component
@ConfigurationProperties(prefix = "remote.client")
public class RemoteClientProperties {

    private int connectTimeout = 5000;
    private int readTimeout = 30000;
    private int retryMaxAttempts = 3;
    private long retryBackoffDelay = 1000;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    public void setRetryMaxAttempts(int retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }

    public long getRetryBackoffDelay() {
        return retryBackoffDelay;
    }

    public void setRetryBackoffDelay(long retryBackoffDelay) {
        this.retryBackoffDelay = retryBackoffDelay;
    }
}

