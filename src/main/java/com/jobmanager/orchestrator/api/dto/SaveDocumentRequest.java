package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * DTO for saving a document to MongoDB.
 */
@Schema(description = "Request to save or update a document in MongoDB")
public class SaveDocumentRequest {

    @Schema(description = "Document key (unique identifier)", example = "user-profile-123", required = true)
    @NotBlank(message = "Document key is required")
    private String documentKey;

    @Schema(description = "Document data as key-value pairs", example = "{\"name\": \"John\", \"age\": 30}", required = true)
    @NotNull(message = "Data is required")
    private Map<String, Object> data;

    public SaveDocumentRequest() {
    }

    public SaveDocumentRequest(String documentKey, Map<String, Object> data) {
        this.documentKey = documentKey;
        this.data = data;
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
}

