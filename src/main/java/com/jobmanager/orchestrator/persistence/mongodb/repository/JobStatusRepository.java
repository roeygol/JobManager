package com.jobmanager.orchestrator.persistence.mongodb.repository;

import com.jobmanager.orchestrator.persistence.mongodb.document.JobStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for JobStatus entities.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface JobStatusRepository extends MongoRepository<JobStatus, String> {

    /**
     * Finds a job status by its UUID.
     *
     * @param uuid the UUID to search for
     * @return Optional containing the job status if found
     */
    Optional<JobStatus> findByUuid(String uuid);

    /**
     * Checks if a job status exists with the given UUID.
     *
     * @param uuid the UUID to check
     * @return true if job status exists, false otherwise
     */
    boolean existsByUuid(String uuid);
}

