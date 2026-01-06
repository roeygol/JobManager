package com.jobmanager.orchestrator.persistence.repository;

import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for JobRestMapping entities.
 * Handles persistence operations for job-to-service mappings.
 */
@Repository
public interface JobRestMappingRepository extends JpaRepository<JobRestMapping, Long> {

    /**
     * Finds a mapping by job name.
     *
     * @param jobName the job name to search for
     * @return Optional containing the mapping if found
     */
    Optional<JobRestMapping> findByJobName(String jobName);
}

