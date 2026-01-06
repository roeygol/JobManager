package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.api.dto.JobRestMappingRequest;
import com.jobmanager.orchestrator.persistence.mongodb.document.JobMapping;
import com.jobmanager.orchestrator.persistence.mongodb.repository.JobMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB-based CRUD service for {@link JobMapping}.
 * This is the Mongo equivalent of the JPA-based JobRestMappingCrudService.
 */
@Service
@Transactional
public class JobMappingMongoService {

    private static final Logger logger = LoggerFactory.getLogger(JobMappingMongoService.class);

    private final JobMappingRepository repository;

    public JobMappingMongoService(JobMappingRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new job mapping in MongoDB.
     *
     * @param request payload with mapping data
     * @return saved document
     */
    public JobMapping create(JobRestMappingRequest request) {
        logger.info("Creating Mongo job mapping for jobName={}", request.getJobName());

        if (repository.existsByJobName(request.getJobName())) {
            throw new DataIntegrityViolationException(
                    "Mapping already exists for jobName=" + request.getJobName());
        }

        JobMapping document = toDocument(new JobMapping(), request);
        return repository.save(document);
    }

    /**
     * Retrieves a mapping by ID.
     */
    @Transactional(readOnly = true)
    public Optional<JobMapping> getById(String id) {
        return repository.findById(id);
    }

    /**
     * Retrieves a mapping by job name.
     */
    @Transactional(readOnly = true)
    public Optional<JobMapping> getByJobName(String jobName) {
        return repository.findByJobName(jobName);
    }

    /**
     * Lists all mappings.
     */
    @Transactional(readOnly = true)
    public List<JobMapping> listAll() {
        return repository.findAll();
    }

    /**
     * Updates a mapping by ID.
     *
     * @throws java.util.NoSuchElementException if mapping does not exist
     * @throws DataIntegrityViolationException  if jobName conflicts
     */
    public JobMapping update(String id, JobRestMappingRequest request) {
        JobMapping existing = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Mongo job mapping not found for id={}", id);
                    return new java.util.NoSuchElementException("JobMapping not found for id=" + id);
                });

        // Ensure unique jobName when updating
        repository.findByJobName(request.getJobName())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(conflict -> {
                    throw new DataIntegrityViolationException(
                            "Mapping already exists for jobName=" + request.getJobName());
                });

        JobMapping toSave = toDocument(existing, request);
        return repository.save(toSave);
    }

    /**
     * Deletes a mapping by ID.
     *
     * @return true if deleted, false if not found
     */
    public boolean deleteById(String id) {
        if (!repository.existsById(id)) {
            logger.warn("Mongo job mapping not found for deletion, id={}", id);
            return false;
        }
        repository.deleteById(id);
        logger.info("Deleted Mongo job mapping with id={}", id);
        return true;
    }

    /**
     * Deletes a mapping by job name.
     *
     * @return true if deleted, false if not found
     */
    public boolean deleteByJobName(String jobName) {
        Optional<JobMapping> mapping = repository.findByJobName(jobName);
        if (mapping.isEmpty()) {
            logger.warn("Mongo job mapping not found for deletion, jobName={}", jobName);
            return false;
        }
        repository.delete(mapping.get());
        logger.info("Deleted Mongo job mapping with jobName={}", jobName);
        return true;
    }

    private JobMapping toDocument(JobMapping document, JobRestMappingRequest request) {
        document.setJobName(request.getJobName());
        document.setServiceName(request.getServiceName());
        document.setUrl(request.getUrl());
        document.setPort(request.getPort());
        return document;
    }
}


