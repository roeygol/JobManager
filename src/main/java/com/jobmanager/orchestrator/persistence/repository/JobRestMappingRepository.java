package com.jobmanager.orchestrator.persistence.repository;

import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for JobRestMapping documents.
 * Handles persistence operations for job-to-service mappings.
 */
@Repository
public interface JobRestMappingRepository extends MongoRepository<JobRestMapping, String> {

    /**
     * Finds a mapping by job name.
     *
     * @param jobName the job name to search for
     * @return Optional containing the mapping if found
     */
    Optional<JobRestMapping> findByJobName(String jobName);

    /**
     * Checks if a mapping exists for the given job name.
     *
     * @param jobName the job name to check
     * @return true if a mapping exists, false otherwise
     */
    boolean existsByJobName(String jobName);

    /**
     * Finds a mapping by the composite unique key: jobName, url, and httpMethod.
     *
     * @param jobName the job name
     * @param url the service URL
     * @param httpMethod the HTTP method
     * @return Optional containing the mapping if found
     */
    Optional<JobRestMapping> findByJobNameAndUrlAndHttpMethod(String jobName, String url, String httpMethod);

    /**
     * Checks if a mapping exists for the composite unique key.
     *
     * @param jobName the job name
     * @param url the service URL
     * @param httpMethod the HTTP method
     * @return true if a mapping exists, false otherwise
     */
    boolean existsByJobNameAndUrlAndHttpMethod(String jobName, String url, String httpMethod);
}

