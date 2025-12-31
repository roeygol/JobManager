package com.jobmanager.orchestrator.integration;

import com.jobmanager.orchestrator.JobOrchestratorApplication;
import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.domain.entity.JobStatus;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import com.jobmanager.orchestrator.persistence.repository.JobRestMappingRepository;
import com.jobmanager.orchestrator.persistence.repository.JobStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for job orchestration flow.
 * Tests the complete lifecycle including creation, status retrieval, and cancellation.
 */
@SpringBootTest(classes = JobOrchestratorApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class JobOrchestrationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JobRestMappingRepository mappingRepository;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    private MockMvc mockMvc;

    @Autowired
    private JobRestMappingRepository mappingRepository;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    private String testJobName;
    private String testIdempotencyKey;
    private JobRestMapping testMapping;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        testJobName = "integration-test-job";
        testIdempotencyKey = "integration-test-key-" + UUID.randomUUID();
        
        // Clean up existing data
        jobStatusRepository.deleteAll();
        mappingRepository.deleteAll();
        
        // Create test mapping
        testMapping = new JobRestMapping(testJobName, "test-service", "http://localhost", 9999);
        mappingRepository.save(testMapping);
    }

    @Test
    void createJob_Success() throws Exception {
        // When/Then
        String uuidString = mockMvc.perform(post("/job/create/{jobName}", testJobName)
                        .header("Idempotency-Key", testIdempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"test\":\"data\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.uuid").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify job status was created
        UUID uuid = extractUuidFromResponse(uuidString);
        assertTrue(jobStatusRepository.findByUuid(uuid).isPresent());
        JobStatus jobStatus = jobStatusRepository.findByUuid(uuid).get();
        assertEquals(testIdempotencyKey, jobStatus.getIdempotencyKey());
        assertEquals(JobExecutionStatus.STARTED, jobStatus.getStatus());
    }

    @Test
    void createJob_IdempotentRequest() throws Exception {
        // Given - Create first job
        String firstResponse = mockMvc.perform(post("/job/create/{jobName}", testJobName)
                        .header("Idempotency-Key", testIdempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"test\":\"data\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID firstUuid = extractUuidFromResponse(firstResponse);

        // When - Create same job with same idempotency key
        String secondResponse = mockMvc.perform(post("/job/create/{jobName}", testJobName)
                        .header("Idempotency-Key", testIdempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"test\":\"data\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID secondUuid = extractUuidFromResponse(secondResponse);

        // Then - Should return same UUID
        assertEquals(firstUuid, secondUuid);
        
        // Verify only one job was created
        assertEquals(1, jobStatusRepository.findByIdempotencyKey(testIdempotencyKey).stream().count());
    }

    @Test
    void createJob_MissingIdempotencyKey() throws Exception {
        // When/Then
        mockMvc.perform(post("/job/create/{jobName}", testJobName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"test\":\"data\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createJob_InvalidJobName() throws Exception {
        // When/Then
        mockMvc.perform(post("/job/create/{jobName}", "")
                        .header("Idempotency-Key", testIdempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createJob_MappingNotFound() throws Exception {
        // When/Then
        mockMvc.perform(post("/job/create/{jobName}", "non-existent-job")
                        .header("Idempotency-Key", testIdempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"test\":\"data\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("JOB_MAPPING_NOT_FOUND"));
    }

    @Test
    void getJobStatus_Success() throws Exception {
        // Given - Create a job
        JobStatus jobStatus = new JobStatus(UUID.randomUUID(), JobExecutionStatus.SUCCESS);
        jobStatus.setResponse("Success response");
        jobStatus.setHttpStatus(200);
        jobStatus = jobStatusRepository.save(jobStatus);

        // When/Then
        mockMvc.perform(get("/job/{uuid}", jobStatus.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(jobStatus.getUuid().toString()))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.response").value("Success response"))
                .andExpect(jsonPath("$.httpStatus").value(200));
    }

    @Test
    void getJobStatus_NotFound() throws Exception {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();

        // When/Then
        mockMvc.perform(get("/job/{uuid}", nonExistentUuid.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("JOB_NOT_FOUND"));
    }

    @Test
    void getJobStatus_InvalidUuidFormat() throws Exception {
        // When/Then
        mockMvc.perform(get("/job/{uuid}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelJob_Success() throws Exception {
        // Given - Create a job in progress
        JobStatus jobStatus = new JobStatus(UUID.randomUUID(), JobExecutionStatus.IN_PROGRESS);
        jobStatus = jobStatusRepository.save(jobStatus);

        // When/Then
        mockMvc.perform(post("/job/cancel/{uuid}", jobStatus.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(jobStatus.getUuid().toString()))
                .andExpect(jsonPath("$.message").exists());

        // Verify job was cancelled
        JobStatus cancelledJob = jobStatusRepository.findByUuid(jobStatus.getUuid()).get();
        assertEquals(JobExecutionStatus.CANCELLED, cancelledJob.getStatus());
        assertNotNull(cancelledJob.getEndDate());
    }

    @Test
    void cancelJob_NotFound() throws Exception {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();

        // When/Then
        mockMvc.perform(post("/job/cancel/{uuid}", nonExistentUuid.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("JOB_NOT_FOUND"));
    }

    @Test
    void cancelJob_AlreadyCompleted_NoOp() throws Exception {
        // Given - Create a completed job
        JobStatus jobStatus = new JobStatus(UUID.randomUUID(), JobExecutionStatus.SUCCESS);
        jobStatus.setEndDate(java.time.LocalDateTime.now());
        jobStatus = jobStatusRepository.save(jobStatus);

        // When/Then
        mockMvc.perform(post("/job/cancel/{uuid}", jobStatus.getUuid().toString()))
                .andExpect(status().isOk());

        // Verify job status unchanged
        JobStatus unchangedJob = jobStatusRepository.findByUuid(jobStatus.getUuid()).get();
        assertEquals(JobExecutionStatus.SUCCESS, unchangedJob.getStatus());
    }

    @Test
    void cancelJob_InvalidUuidFormat() throws Exception {
        // When/Then
        mockMvc.perform(post("/job/cancel/{uuid}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    // Helper method to extract UUID from JSON response
    private UUID extractUuidFromResponse(String response) {
        try {
            // Extract UUID from JSON response: {"uuid":"550e8400-e29b-41d4-a716-446655440000"}
            int uuidStart = response.indexOf("\"uuid\":\"") + 8;
            int uuidEnd = response.indexOf("\"", uuidStart);
            if (uuidStart > 7 && uuidEnd > uuidStart) {
                String uuidString = response.substring(uuidStart, uuidEnd);
                return UUID.fromString(uuidString);
            }
            throw new IllegalArgumentException("Could not extract UUID from response: " + response);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid UUID format in response: " + response, e);
        }
    }
}

