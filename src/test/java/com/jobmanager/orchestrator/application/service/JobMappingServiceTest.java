package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.domain.exception.JobMappingNotFoundException;
import com.jobmanager.orchestrator.persistence.repository.JobRestMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobMappingService.
 */
@ExtendWith(MockitoExtension.class)
class JobMappingServiceTest {

    @Mock
    private JobRestMappingRepository mappingRepository;

    @InjectMocks
    private JobMappingService mappingService;

    private JobRestMapping testMapping;
    private String testJobName;

    @BeforeEach
    void setUp() {
        testJobName = "test-job";
        testMapping = new JobRestMapping(testJobName, "test-service", "http://localhost", 8080);
    }

    @Test
    void resolveMapping_Success() {
        // Given
        when(mappingRepository.findByJobName(testJobName)).thenReturn(Optional.of(testMapping));

        // When
        JobRestMapping result = mappingService.resolveMapping(testJobName);

        // Then
        assertNotNull(result);
        assertEquals(testJobName, result.getJobName());
        verify(mappingRepository).findByJobName(testJobName);
    }

    @Test
    void resolveMapping_NotFound() {
        // Given
        when(mappingRepository.findByJobName(testJobName)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(JobMappingNotFoundException.class, 
                () -> mappingService.resolveMapping(testJobName));
    }
}

