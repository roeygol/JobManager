package com.jobmanager.orchestrator.domain.exception;

/**
 * Exception thrown when a job mapping cannot be found.
 */
public class JobMappingNotFoundException extends RuntimeException {

    public JobMappingNotFoundException(String message) {
        super(message);
    }

    public JobMappingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

