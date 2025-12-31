package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.domain.entity.JobStatus;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import com.jobmanager.orchestrator.domain.exception.JobMappingNotFoundException;
import com.jobmanager.orchestrator.domain.exception.JobNotFoundException;
import com.jobmanager.orchestrator.persistence.repository.JobStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobOrchestrationService.
 */
@ExtendWith(MockitoExtension.class)
class JobOrchestrationServiceTest {

    @Mock
    private JobMappingService mappingService;

    @Mock
    private HttpForwardingService httpForwardingService;

    @Mock
    private JobStatusRepository jobStatusRepository;

    @InjectMocks
    private JobOrchestrationService orchestrationService;

    private JobRestMapping testMapping;
    private String testJobName;
    private String testIdempotencyKey;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testJobName = "test-job";
        testIdempotencyKey = "test-idempotency-key-123";
        testUuid = UUID.randomUUID();
        testMapping = new JobRestMapping(testJobName, "test-service", "http://localhost", 8080);
    }

    @Test
    void createAndTriggerJob_Success() {
        // Given
        when(mappingService.resolveMapping(testJobName)).thenReturn(testMapping);
        when(jobStatusRepository.findByIdempotencyKey(testIdempotencyKey)).thenReturn(Optional.empty());
        when(jobStatusRepository.save(any(JobStatus.class))).thenAnswer(invocation -> {
            JobStatus status = invocation.getArgument(0);
            status.setId(1L);
            return status;
        });

        // When
        UUID uuid = orchestrationService.createAndTriggerJob(
                testJobName, testIdempotencyKey, HttpMethod.POST, 
                new HashMap<>(), new HashMap<>(), "{}");

        // Then
        assertNotNull(uuid);
        verify(mappingService).resolveMapping(testJobName);
        verify(jobStatusRepository).findByIdempotencyKey(testIdempotencyKey);
        verify(jobStatusRepository).save(any(JobStatus.class));
    }

    @Test
    void createAndTriggerJob_IdempotentRequest() {
        // Given
        JobStatus existingJob = new JobStatus(testUuid, JobExecutionStatus.IN_PROGRESS);
        existingJob.setIdempotencyKey(testIdempotencyKey);
        when(jobStatusRepository.findByIdempotencyKey(testIdempotencyKey))
                .thenReturn(Optional.of(existingJob));

        // When/Then
        assertThrows(JobOrchestrationService.IdempotentRequestException.class, 
                () -> orchestrationService.createAndTriggerJob(
                        testJobName, testIdempotencyKey, HttpMethod.POST, 
                        new HashMap<>(), new HashMap<>(), "{}"));
        
        verify(jobStatusRepository).findByIdempotencyKey(testIdempotencyKey);
        verify(mappingService, never()).resolveMapping(any());
    }

    @Test
    void createAndTriggerJob_MappingNotFound() {
        // Given
        when(jobStatusRepository.findByIdempotencyKey(testIdempotencyKey)).thenReturn(Optional.empty());
        when(mappingService.resolveMapping(testJobName))
                .thenThrow(new JobMappingNotFoundException("Mapping not found"));

        // When/Then
        assertThrows(JobMappingNotFoundException.class, 
                () -> orchestrationService.createAndTriggerJob(
                        testJobName, testIdempotencyKey, HttpMethod.POST, 
                        new HashMap<>(), new HashMap<>(), "{}"));
    }

    @Test
    void getJobStatus_Success() {
        // Given
        JobStatus jobStatus = new JobStatus(testUuid, JobExecutionStatus.SUCCESS);
        when(jobStatusRepository.findByUuid(testUuid)).thenReturn(Optional.of(jobStatus));

        // When
        JobStatus result = orchestrationService.getJobStatus(testUuid);

        // Then
        assertNotNull(result);
        assertEquals(testUuid, result.getUuid());
        assertEquals(JobExecutionStatus.SUCCESS, result.getStatus());
        verify(jobStatusRepository).findByUuid(testUuid);
    }

    @Test
    void getJobStatus_NotFound() {
        // Given
        when(jobStatusRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(JobNotFoundException.class, 
                () -> orchestrationService.getJobStatus(testUuid));
    }

    @Test
    void cancelJob_Success() {
        // Given
        JobStatus jobStatus = new JobStatus(testUuid, JobExecutionStatus.IN_PROGRESS);
        when(jobStatusRepository.findByUuid(testUuid)).thenReturn(Optional.of(jobStatus));
        when(jobStatusRepository.save(any(JobStatus.class))).thenReturn(jobStatus);

        // Get the execution registry and add a mock future
        @SuppressWarnings("unchecked")
        Map<UUID, Future<?>> registry = (Map<UUID, Future<?>>) 
                ReflectionTestUtils.getField(orchestrationService, "executionRegistry");
        Future<?> mockFuture = mock(Future.class);
        when(mockFuture.cancel(true)).thenReturn(true);
        registry.put(testUuid, mockFuture);

        // When
        orchestrationService.cancelJob(testUuid);

        // Then
        verify(jobStatusRepository).findByUuid(testUuid);
        verify(jobStatusRepository).save(any(JobStatus.class));
        ArgumentCaptor<JobStatus> statusCaptor = ArgumentCaptor.forClass(JobStatus.class);
        verify(jobStatusRepository).save(statusCaptor.capture());
        assertEquals(JobExecutionStatus.CANCELLED, statusCaptor.getValue().getStatus());
        verify(mockFuture).cancel(true);
    }

    @Test
    void cancelJob_NotFound() {
        // Given
        when(jobStatusRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(JobNotFoundException.class, 
                () -> orchestrationService.cancelJob(testUuid));
    }

    @Test
    void cancelJob_AlreadyCompleted_NoOp() {
        // Given
        JobStatus jobStatus = new JobStatus(testUuid, JobExecutionStatus.SUCCESS);
        jobStatus.setEndDate(LocalDateTime.now());
        when(jobStatusRepository.findByUuid(testUuid)).thenReturn(Optional.of(jobStatus));

        // When
        orchestrationService.cancelJob(testUuid);

        // Then
        verify(jobStatusRepository).findByUuid(testUuid);
        verify(jobStatusRepository, never()).save(any(JobStatus.class));
    }

    @Test
    void cancelJob_AlreadyCancelled_NoOp() {
        // Given
        JobStatus jobStatus = new JobStatus(testUuid, JobExecutionStatus.CANCELLED);
        jobStatus.setEndDate(LocalDateTime.now());
        when(jobStatusRepository.findByUuid(testUuid)).thenReturn(Optional.of(jobStatus));

        // When
        orchestrationService.cancelJob(testUuid);

        // Then
        verify(jobStatusRepository).findByUuid(testUuid);
        verify(jobStatusRepository, never()).save(any(JobStatus.class));
    }
}
