package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for document deletion response.
 */
@Schema(description = "Response for document deletion operation")
public class DeleteDocumentResponse {

    @Schema(description = "Indicates if the document was deleted", example = "true")
    private boolean deleted;

    @Schema(description = "Message describing the result", example = "Document deleted successfully")
    private String message;

    @Schema(description = "Document key that was deleted", example = "user-profile-123")
    private String documentKey;

    public DeleteDocumentResponse() {
    }

    public DeleteDocumentResponse(boolean deleted, String message, String documentKey) {
        this.deleted = deleted;
        this.message = message;
        this.documentKey = documentKey;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public void setDocumentKey(String documentKey) {
        this.documentKey = documentKey;
    }
}

