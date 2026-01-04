package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for MongoDB document response.
 */
@Schema(description = "Response containing MongoDB document information")
public class DocumentResponse {

    @Schema(description = "Document ID", example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(description = "Document key", example = "user-profile-123")
    private String documentKey;

    @Schema(description = "Document data", example = "{\"name\": \"John\", \"age\": 30}")
    private Map<String, Object> data;

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    public DocumentResponse() {
    }

    public DocumentResponse(String id, String documentKey, Map<String, Object> data, 
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.documentKey = documentKey;
        this.data = data;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public void setDocumentKey(String documentKey) {
        this.documentKey = documentKey;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

