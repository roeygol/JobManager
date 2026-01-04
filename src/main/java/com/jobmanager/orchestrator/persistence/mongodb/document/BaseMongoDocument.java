package com.jobmanager.orchestrator.persistence.mongodb.document;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * Base abstract class for MongoDB document entities.
 * Provides common fields and functionality for all MongoDB documents.
 * This follows best practices for code reuse and consistency across document collections.
 */
public abstract class BaseMongoDocument {

    @Id
    private String id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected BaseMongoDocument() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    /**
     * Updates the updatedAt timestamp.
     * Should be called whenever the document is modified.
     */
    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}

