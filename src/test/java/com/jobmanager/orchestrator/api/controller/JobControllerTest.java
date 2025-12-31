package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.CancelJobResponse;
import com.jobmanager.orchestrator.api.dto.JobExecutionResponse;
import com.jobmanager.orchestrator.api.dto.JobStatusResponse;
import com.jobmanager.orchestrator.api.mapper.JobStatusMapper;
import com.jobmanager.orchestrator.application.service.JobOrchestrationService;
import com.jobmanager.orchestrator.domain.entity.JobStatus;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import com.jobmanager.orchestrator.domain.exception.JobMappingNotFoundException;
import com.jobmanager.orchestrator.domain.exception.JobNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobController.
 */
@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private JobOrchestrationService orchestrationService;

    @Mock
    private JobStatusMapper statusMapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private JobController jobController;

    private UUID testUuid;
    private String testJobName;
    private String testIdempotencyKey;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        testJobName = "test-job";
        testIdempotencyKey = "test-idempotency-key-123";
    }

    @Test
    void createJob_Success() throws Exception {
        // Given
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletRequest.getHeader("Idempotency-Key")).thenReturn(testIdempotencyKey);
        when(httpServletRequest.getHeaderNames()).thenReturn(createEnumeration("Content-Type", "Accept"));
        when(httpServletRequest.getHeader("Content-Type")).thenReturn("application/json");
        when(httpServletRequest.getHeader("Accept")).thenReturn("application/json");
        when(httpServletRequest.getParameterNames()).thenReturn(createEnumeration());
        when(httpServletRequest.getContentType()).thenReturn("application/json");
        when(httpServletRequest.getInputStream()).thenReturn(new MockServletInputStream("{\"key\":\"value\"}"));
        
        when(orchestrationService.createAndTriggerJob(
                eq(testJobName), eq(testIdempotencyKey), any(), any(), any(), any()))
                .thenReturn(testUuid);

        // When
        ResponseEntity<JobExecutionResponse> response = jobController.createJob(
                testJobName, testIdempotencyKey, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUuid, response.getBody().getUuid());
        verify(orchestrationService).createAndTriggerJob(
                eq(testJobName), eq(testIdempotencyKey), any(), any(), any(), any());
    }

    @Test
    void createJob_IdempotentRequest() throws Exception {
        // Given
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletRequest.getHeader("Idempotency-Key")).thenReturn(testIdempotencyKey);
        when(httpServletRequest.getHeaderNames()).thenReturn(createEnumeration());
        when(httpServletRequest.getParameterNames()).thenReturn(createEnumeration());
        when(httpServletRequest.getContentType()).thenReturn(null);
        
        UUID existingUuid = UUID.randomUUID();
        JobOrchestrationService.IdempotentRequestException idempotentException = 
                new JobOrchestrationService.IdempotentRequestException(existingUuid);
        
        when(orchestrationService.createAndTriggerJob(
                eq(testJobName), eq(testIdempotencyKey), any(), any(), any(), any()))
                .thenThrow(idempotentException);

        // When
        ResponseEntity<JobExecutionResponse> response = jobController.createJob(
                testJobName, testIdempotencyKey, httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(existingUuid, response.getBody().getUuid());
    }

    @Test
    void createJob_MappingNotFound() throws Exception {
        // Given
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletRequest.getHeader("Idempotency-Key")).thenReturn(testIdempotencyKey);
        when(httpServletRequest.getHeaderNames()).thenReturn(createEnumeration());
        when(httpServletRequest.getParameterNames()).thenReturn(createEnumeration());
        when(httpServletRequest.getContentType()).thenReturn(null);
        
        when(orchestrationService.createAndTriggerJob(
                eq(testJobName), eq(testIdempotencyKey), any(), any(), any(), any()))
                .thenThrow(new JobMappingNotFoundException("Mapping not found"));

        // When/Then
        assertThrows(JobMappingNotFoundException.class, 
                () -> jobController.createJob(testJobName, testIdempotencyKey, httpServletRequest));
    }

    @Test
    void getJobStatus_Success() {
        // Given
        String uuidString = testUuid.toString();
        JobStatus jobStatus = new JobStatus(testUuid, JobExecutionStatus.SUCCESS);
        jobStatus.setStartDate(LocalDateTime.now());
        jobStatus.setEndDate(LocalDateTime.now());
        jobStatus.setResponse("Success response");
        jobStatus.setHttpStatus(200);

        JobStatusResponse statusResponse = new JobStatusResponse();
        statusResponse.setUuid(testUuid);
        statusResponse.setStatus(JobExecutionStatus.SUCCESS);
        statusResponse.setResponse("Success response");
        statusResponse.setHttpStatus(200);

        when(orchestrationService.getJobStatus(testUuid)).thenReturn(jobStatus);
        when(statusMapper.toDto(jobStatus)).thenReturn(statusResponse);

        // When
        ResponseEntity<JobStatusResponse> response = jobController.getJobStatus(uuidString);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUuid, response.getBody().getUuid());
        assertEquals(JobExecutionStatus.SUCCESS, response.getBody().getStatus());
        verify(orchestrationService).getJobStatus(testUuid);
        verify(statusMapper).toDto(jobStatus);
    }

    @Test
    void getJobStatus_NotFound() {
        // Given
        String uuidString = testUuid.toString();
        when(orchestrationService.getJobStatus(testUuid))
                .thenThrow(new JobNotFoundException("Job not found"));

        // When/Then
        assertThrows(JobNotFoundException.class, 
                () -> jobController.getJobStatus(uuidString));
    }

    @Test
    void cancelJob_Success() {
        // Given
        String uuidString = testUuid.toString();
        doNothing().when(orchestrationService).cancelJob(testUuid);

        // When
        ResponseEntity<CancelJobResponse> response = jobController.cancelJob(uuidString);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUuid, response.getBody().getUuid());
        assertNotNull(response.getBody().getMessage());
        verify(orchestrationService).cancelJob(testUuid);
    }

    @Test
    void cancelJob_NotFound() {
        // Given
        String uuidString = testUuid.toString();
        doThrow(new JobNotFoundException("Job not found"))
                .when(orchestrationService).cancelJob(testUuid);

        // When/Then
        assertThrows(JobNotFoundException.class, 
                () -> jobController.cancelJob(uuidString));
    }

    // Helper methods
    private Enumeration<String> createEnumeration(String... elements) {
        return new Enumeration<String>() {
            private int index = 0;

            @Override
            public boolean hasMoreElements() {
                return index < elements.length;
            }

            @Override
            public String nextElement() {
                return elements[index++];
            }
        };
    }

    // Mock ServletInputStream
    private static class MockServletInputStream extends jakarta.servlet.ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public MockServletInputStream(String content) {
            this.inputStream = new ByteArrayInputStream(content.getBytes());
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(jakarta.servlet.ReadListener readListener) {
            // Not needed for testing
        }
    }
}
