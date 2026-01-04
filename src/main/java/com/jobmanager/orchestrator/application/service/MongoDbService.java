package com.jobmanager.orchestrator.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmanager.orchestrator.domain.enums.JobExecutionStatus;
import com.jobmanager.orchestrator.persistence.mongodb.document.JobStatus;
import com.jobmanager.orchestrator.persistence.mongodb.repository.JobStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for MongoDB job status operations.
 * Provides methods to store and retrieve job status information from MongoDB.
 */
@Service
public class MongoDbService {

    private static final Logger logger = LoggerFactory.getLogger(MongoDbService.class);

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Retrieves a job status by its ID.
     *
     * @param id the job status ID
     * @return Optional containing the job status if found
     */
    public Optional<JobStatus> getById(String id) {
        logger.debug("Retrieving job status by ID: {}", id);
        return jobStatusRepository.findById(id);
    }

    /**
     * Retrieves a job status by its UUID.
     *
     * @param uuid the UUID
     * @return Optional containing the job status if found
     */
    public Optional<JobStatus> getByUuid(String uuid) {
        logger.debug("Retrieving job status by UUID: {}", uuid);
        return jobStatusRepository.findByUuid(uuid);
    }

    /**
     * Retrieves all job statuses from the database.
     *
     * @return list of all job statuses
     */
    public List<JobStatus> getAllJobStatuses() {
        logger.debug("Retrieving all job statuses");
        return jobStatusRepository.findAll();
    }

    /**
     * Retrieves a job status as JSON string.
     *
     * @param uuid the UUID
     * @return Optional containing the JSON string representation if job status found
     */
    public Optional<String> getJobStatusAsJsonString(String uuid) {
        logger.debug("Retrieving job status as JSON string by UUID: {}", uuid);
        return getByUuid(uuid)
                .map(jobStatus -> {
                    try {
                        return objectMapper.writeValueAsString(jobStatus);
                    } catch (Exception e) {
                        logger.error("Failed to convert job status to JSON string", e);
                        return null;
                    }
                });
    }

    /**
     * Checks if a job status exists with the given UUID.
     *
     * @param uuid the UUID to check
     * @return true if job status exists, false otherwise
     */
    public boolean jobStatusExists(String uuid) {
        logger.debug("Checking if job status exists with UUID: {}", uuid);
        return jobStatusRepository.existsByUuid(uuid);
    }

    /**
     * Saves or updates a job status in MongoDB.
     *
     * @param uuid the UUID
     * @param status the job execution status
     * @param response the response message
     * @param startDate the start date
     * @param endDate the end date
     * @param httpCode the HTTP status code
     * @return the saved job status
     */
    public JobStatus saveJobStatus(String uuid, JobExecutionStatus status, String response,
                                    LocalDateTime startDate, LocalDateTime endDate, Integer httpCode) {
        logger.info("Saving job status with UUID: {}", uuid);
        
        JobStatus jobStatus = jobStatusRepository.findByUuid(uuid)
                .orElse(new JobStatus());
        
        jobStatus.setUuid(uuid);
        jobStatus.setStatus(status);
        jobStatus.setResponse(response);
        jobStatus.setStartDate(startDate);
        jobStatus.setEndDate(endDate);
        jobStatus.setHttpCode(httpCode);
        
        JobStatus saved = jobStatusRepository.save(jobStatus);
        logger.info("Job status saved with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Deletes a job status by its UUID.
     *
     * @param uuid the UUID
     * @return true if job status was deleted, false if not found
     */
    public boolean deleteJobStatus(String uuid) {
        logger.info("Deleting job status with UUID: {}", uuid);
        Optional<JobStatus> jobStatus = jobStatusRepository.findByUuid(uuid);
        if (jobStatus.isPresent()) {
            jobStatusRepository.delete(jobStatus.get());
            logger.info("Job status deleted with UUID: {}", uuid);
            return true;
        }
        logger.warn("Job status not found for deletion with UUID: {}", uuid);
        return false;
    }

    /**
     * Deletes a job status by its ID.
     *
     * @param id the job status ID
     * @return true if job status was deleted, false if not found
     */
    public boolean deleteJobStatusById(String id) {
        logger.info("Deleting job status with ID: {}", id);
        if (jobStatusRepository.existsById(id)) {
            jobStatusRepository.deleteById(id);
            logger.info("Job status deleted with ID: {}", id);
            return true;
        }
        logger.warn("Job status not found for deletion with ID: {}", id);
        return false;
    }

    /**
     * Converts a JobStatus object to JSON string.
     *
     * @param jobStatus the job status to convert
     * @return JSON string representation
     */
    public String toJsonString(JobStatus jobStatus) {
        try {
            return objectMapper.writeValueAsString(jobStatus);
        } catch (Exception e) {
            logger.error("Failed to convert job status to JSON string", e);
            throw new RuntimeException("Failed to convert job status to JSON string", e);
        }
    }

    /**
     * Converts a JSON string to JobStatus object.
     *
     * @param jsonString the JSON string to parse
     * @return JobStatus object
     */
    public JobStatus fromJsonString(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, JobStatus.class);
        } catch (Exception e) {
            logger.error("Failed to parse JSON string to job status", e);
            throw new RuntimeException("Failed to parse JSON string to job status", e);
        }
    }
}

