package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.domain.entity.JobRestMapping;
import com.jobmanager.orchestrator.domain.exception.JobMappingNotFoundException;
import com.jobmanager.orchestrator.persistence.repository.JobRestMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for job mapping lookup and validation.
 * Encapsulates mapping resolution logic.
 */
@Service
@Transactional(readOnly = true)
public class JobMappingService {

    private static final Logger logger = LoggerFactory.getLogger(JobMappingService.class);

    private final JobRestMappingRepository mappingRepository;

    public JobMappingService(JobRestMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    /**
     * Resolves a job mapping by job name.
     *
     * @param jobName the job name to resolve
     * @return the JobRestMapping entity
     * @throws JobMappingNotFoundException if no mapping exists for the job name
     */
    public JobRestMapping resolveMapping(String jobName) {
        logger.debug("Resolving mapping for job name: {}", jobName);
        
        return mappingRepository.findByJobName(jobName)
                .orElseThrow(() -> {
                    logger.warn("Job mapping not found for job name: {}", jobName);
                    return new JobMappingNotFoundException("No mapping found for job name: " + jobName);
                });
    }
}

