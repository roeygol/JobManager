package com.jobmanager.orchestrator.domain.exception;

/**
 * Exception thrown when a job status cannot be found.
 */
public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String message) {
        super(message);
    }

    public JobNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

