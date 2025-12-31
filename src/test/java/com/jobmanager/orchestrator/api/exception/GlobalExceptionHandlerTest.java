package com.jobmanager.orchestrator.api.exception;

import com.jobmanager.orchestrator.domain.exception.JobMappingNotFoundException;
import com.jobmanager.orchestrator.domain.exception.JobNotFoundException;
import com.jobmanager.orchestrator.domain.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void handleJobMappingNotFound() {
        // Given
        JobMappingNotFoundException exception = new JobMappingNotFoundException("Mapping not found");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleJobMappingNotFound(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JOB_MAPPING_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("Mapping not found", response.getBody().getMessage());
    }

    @Test
    void handleJobNotFound() {
        // Given
        JobNotFoundException exception = new JobNotFoundException("Job not found");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleJobNotFound(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JOB_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("Job not found", response.getBody().getMessage());
    }

    @Test
    void handleValidationException() {
        // Given
        ValidationException exception = new ValidationException("Validation failed");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleValidationException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = 
                mock(org.springframework.validation.BindingResult.class);
        org.springframework.validation.FieldError fieldError = 
                mock(org.springframework.validation.FieldError.class);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                java.util.Collections.singletonList(fieldError));
        when(fieldError.getField()).thenReturn("jobName");
        when(fieldError.getDefaultMessage()).thenReturn("Job name is required");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleValidationException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Request validation failed"));
    }

    @Test
    void handleConstraintViolationException() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid UUID format");
        violations.add(violation);
        
        ConstraintViolationException exception = new ConstraintViolationException(
                "Validation failed", violations);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleConstraintViolationException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Invalid UUID format"));
    }

    @Test
    void handleGenericException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}

