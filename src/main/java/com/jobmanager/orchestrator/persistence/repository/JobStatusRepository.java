package com.jobmanager.orchestrator.persistence.repository;

import com.jobmanager.orchestrator.domain.entity.JobStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for JobStatus entities.
 * Handles persistence operations for job execution status.
 */
@Repository
public interface JobStatusRepository extends CrudRepository<JobStatus, Long> {

    /**
     * Finds a job status by its UUID.
     *
     * @param uuid the UUID to search for
     * @return Optional containing the job status if found
     */
    Optional<JobStatus> findByUuid(UUID uuid);
}

