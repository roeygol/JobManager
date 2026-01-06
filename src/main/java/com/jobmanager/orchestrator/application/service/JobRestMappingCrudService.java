package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.api.dto.JobRestMappingRequest;
import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.persistence.repository.JobRestMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service providing CRUD operations for {@link JobRestMapping}.
 * Encapsulates repository access and business rules (e.g. unique jobName).
 */
@Service
@Transactional
public class JobRestMappingCrudService {

    private static final Logger logger = LoggerFactory.getLogger(JobRestMappingCrudService.class);

    private final JobRestMappingRepository repository;

    public JobRestMappingCrudService(JobRestMappingRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new job mapping.
     *
     * @param request payload with mapping data
     * @return saved entity
     */
    public JobRestMapping create(JobRestMappingRequest request) {
        logger.info("Creating job mapping for jobName={}", request.getJobName());

        if (repository.existsByJobName(request.getJobName())) {
            throw new DataIntegrityViolationException(
                    "Mapping already exists for jobName=" + request.getJobName());
        }

        JobRestMapping entity = toEntity(new JobRestMapping(), request);
        return repository.save(entity);
    }

    /**
     * Retrieves a mapping by ID.
     */
    @Transactional(readOnly = true)
    public Optional<JobRestMapping> getById(Long id) {
        return repository.findById(id);
    }

    /**
     * Retrieves a mapping by job name.
     */
    @Transactional(readOnly = true)
    public Optional<JobRestMapping> getByJobName(String jobName) {
        return repository.findByJobName(jobName);
    }

    /**
     * Lists all mappings.
     */
    @Transactional(readOnly = true)
    public List<JobRestMapping> listAll() {
        return repository.findAll();
    }

    /**
     * Updates a mapping by ID.
     *
     * @throws java.util.NoSuchElementException if mapping does not exist
     * @throws DataIntegrityViolationException  if jobName conflicts
     */
    public JobRestMapping update(Long id, JobRestMappingRequest request) {
        JobRestMapping existing = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Job mapping not found for id={}", id);
                    return new java.util.NoSuchElementException("JobRestMapping not found for id=" + id);
                });

        // Ensure unique jobName when updating
        repository.findByJobName(request.getJobName())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(conflict -> {
                    throw new DataIntegrityViolationException(
                            "Mapping already exists for jobName=" + request.getJobName());
                });

        JobRestMapping toSave = toEntity(existing, request);
        return repository.save(toSave);
    }

    /**
     * Deletes a mapping by ID.
     *
     * @return true if deleted, false if not found
     */
    public boolean deleteById(Long id) {
        if (!repository.existsById(id)) {
            logger.warn("Job mapping not found for deletion, id={}", id);
            return false;
        }
        repository.deleteById(id);
        logger.info("Deleted job mapping with id={}", id);
        return true;
    }

    /**
     * Deletes a mapping by job name.
     *
     * @return true if deleted, false if not found
     */
    public boolean deleteByJobName(String jobName) {
        Optional<JobRestMapping> mapping = repository.findByJobName(jobName);
        if (mapping.isEmpty()) {
            logger.warn("Job mapping not found for deletion, jobName={}", jobName);
            return false;
        }
        repository.delete(mapping.get());
        logger.info("Deleted job mapping with jobName={}", jobName);
        return true;
    }

    private JobRestMapping toEntity(JobRestMapping entity, JobRestMappingRequest request) {
        entity.setJobName(request.getJobName());
        entity.setServiceName(request.getServiceName());
        entity.setUrl(request.getUrl());
        entity.setPort(request.getPort());
        return entity;
    }
}


