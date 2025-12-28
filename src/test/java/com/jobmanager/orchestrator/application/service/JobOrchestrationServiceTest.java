package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.domain.entity.JobStatus;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import com.jobmanager.orchestrator.domain.exception.JobMappingNotFoundException;
import com.jobmanager.orchestrator.persistence.repository.JobStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobOrchestrationService.
 */
@ExtendWith(MockitoExtension.class)
class JobOrchestrationServiceTest {

    @Mock
    private JobMappingService mappingService;

    @Mock
    private RemoteJobClient remoteJobClient;

    @Mock
    private JobStatusRepository jobStatusRepository;

    @InjectMocks
    private JobOrchestrationService orchestrationService;

    private JobRestMapping testMapping;
    private String testJobName;

    @BeforeEach
    void setUp() {
        testJobName = "test-job";
        testMapping = new JobRestMapping(testJobName, "test-service", "http://localhost", 8080);
    }

    @Test
    void createAndTriggerJob_Success() {
        // Given
        when(mappingService.resolveMapping(testJobName)).thenReturn(testMapping);
        when(jobStatusRepository.save(any(JobStatus.class))).thenAnswer(invocation -> {
            JobStatus status = invocation.getArgument(0);
            status.setId(1L);
            return status;
        });

        // When
        UUID uuid = orchestrationService.createAndTriggerJob(testJobName);

        // Then
        assertNotNull(uuid);
        verify(mappingService).resolveMapping(testJobName);
        verify(jobStatusRepository, atLeastOnce()).save(any(JobStatus.class));
    }

    @Test
    void createAndTriggerJob_MappingNotFound() {
        // Given
        when(mappingService.resolveMapping(testJobName))
                .thenThrow(new JobMappingNotFoundException("Mapping not found"));

        // When/Then
        assertThrows(JobMappingNotFoundException.class, 
                () -> orchestrationService.createAndTriggerJob(testJobName));
    }

    @Test
    void getJobStatus_Success() {
        // Given
        UUID uuid = UUID.randomUUID();
        JobStatus jobStatus = new JobStatus(uuid, JobExecutionStatus.SUCCESS);
        when(jobStatusRepository.findByUuid(uuid)).thenReturn(Optional.of(jobStatus));

        // When
        JobStatus result = orchestrationService.getJobStatus(uuid);

        // Then
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(JobExecutionStatus.SUCCESS, result.getStatus());
    }

    @Test
    void getJobStatus_NotFound() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(jobStatusRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(com.jobmanager.orchestrator.domain.exception.JobNotFoundException.class,
                () -> orchestrationService.getJobStatus(uuid));
    }
}

