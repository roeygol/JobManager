package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO for database connection test response.
 */
@Schema(description = "Response containing database connection test results")
public class DbConnectionTestResponse {

    @Schema(description = "Indicates if the database connection is successful", example = "true")
    private boolean connected;

    @Schema(description = "Current date/time from the database", example = "2024-01-01T10:00:00")
    private LocalDateTime currentDate;

    @Schema(description = "Message describing the connection status", example = "Database connection successful")
    private String message;

    @Schema(description = "Timestamp when the test was performed", example = "2024-01-01T10:00:00")
    private LocalDateTime testTimestamp;

    public DbConnectionTestResponse() {
    }

    public DbConnectionTestResponse(boolean connected, LocalDateTime currentDate, String message) {
        this.connected = connected;
        this.currentDate = currentDate;
        this.message = message;
        this.testTimestamp = LocalDateTime.now();
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public LocalDateTime getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDateTime currentDate) {
        this.currentDate = currentDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTestTimestamp() {
        return testTimestamp;
    }

    public void setTestTimestamp(LocalDateTime testTimestamp) {
        this.testTimestamp = testTimestamp;
    }
}

