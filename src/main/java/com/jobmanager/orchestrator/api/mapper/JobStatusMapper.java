package com.jobmanager.orchestrator.api.mapper;

import com.jobmanager.orchestrator.api.dto.JobStatusResponse;
import com.jobmanager.orchestrator.domain.entity.JobStatus;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting JobStatus entities to DTOs.
 * Prevents entity leakage to the API layer.
 */
@Component
public class JobStatusMapper {

    public JobStatusResponse toDto(JobStatus entity) {
        if (entity == null) {
            return null;
        }

        JobStatusResponse dto = new JobStatusResponse();
        dto.setUuid(entity.getUuid());
        dto.setStatus(entity.getStatus());
        dto.setResponse(entity.getResponse());
        dto.setHttpStatus(entity.getHttpStatus());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());

        return dto;
    }
}

