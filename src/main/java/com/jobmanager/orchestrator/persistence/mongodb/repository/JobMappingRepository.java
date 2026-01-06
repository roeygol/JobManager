package com.jobmanager.orchestrator.persistence.mongodb.repository;

import com.jobmanager.orchestrator.persistence.mongodb.document.JobMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for {@link JobMapping} documents.
 * Provides CRUD operations and jobName-based lookup for job mappings.
 */
@Repository
public interface JobMappingRepository extends MongoRepository<JobMapping, String> {

    /**
     * Finds a mapping by job name.
     *
     * @param jobName the job name to search for
     * @return Optional containing the mapping if found
     */
    Optional<JobMapping> findByJobName(String jobName);

    /**
     * Checks if a mapping exists for the given job name.
     *
     * @param jobName the job name to check
     * @return true if a mapping exists, false otherwise
     */
    boolean existsByJobName(String jobName);
}


