package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.JobExecutionRequest;
import com.jobmanager.orchestrator.api.dto.JobExecutionResponse;
import com.jobmanager.orchestrator.api.dto.JobStatusResponse;
import com.jobmanager.orchestrator.api.mapper.JobStatusMapper;
import com.jobmanager.orchestrator.application.service.JobOrchestrationService;
import com.jobmanager.orchestrator.domain.entity.JobStatus;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import com.jobmanager.orchestrator.domain.exception.JobMappingNotFoundException;
import com.jobmanager.orchestrator.domain.exception.JobNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private JobController jobController;

    private UUID testUuid;
    private JobExecutionRequest testRequest;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        testRequest = new JobExecutionRequest("test-job");
    }

    @Test
    void createJob_Success() {
        // Given
        when(orchestrationService.createAndTriggerJob("test-job")).thenReturn(testUuid);

        // When
        ResponseEntity<JobExecutionResponse> response = jobController.createJob(testRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUuid, response.getBody().getUuid());
        verify(orchestrationService).createAndTriggerJob("test-job");
    }

    @Test
    void createJob_MappingNotFound() {
        // Given
        when(orchestrationService.createAndTriggerJob("test-job"))
                .thenThrow(new JobMappingNotFoundException("Mapping not found"));

        // When/Then
        assertThrows(JobMappingNotFoundException.class, 
                () -> jobController.createJob(testRequest));
    }

    @Test
    void getJobStatus_Success() {
        // Given
        JobStatus jobStatus = new JobStatus(testUuid, JobExecutionStatus.SUCCESS);
        jobStatus.setStartDate(LocalDateTime.now());
        jobStatus.setEndDate(LocalDateTime.now());

        JobStatusResponse statusResponse = new JobStatusResponse();
        statusResponse.setUuid(testUuid);
        statusResponse.setStatus(JobExecutionStatus.SUCCESS);

        when(orchestrationService.getJobStatus(testUuid)).thenReturn(jobStatus);
        when(statusMapper.toDto(jobStatus)).thenReturn(statusResponse);

        // When
        ResponseEntity<JobStatusResponse> response = jobController.getJobStatus(testUuid);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUuid, response.getBody().getUuid());
        verify(orchestrationService).getJobStatus(testUuid);
        verify(statusMapper).toDto(jobStatus);
    }

    @Test
    void getJobStatus_NotFound() {
        // Given
        when(orchestrationService.getJobStatus(testUuid))
                .thenThrow(new JobNotFoundException("Job not found"));

        // When/Then
        assertThrows(JobNotFoundException.class, 
                () -> jobController.getJobStatus(testUuid));
    }
}

