package com.jobmanager.orchestrator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO for MongoDB connection test response.
 */
@Schema(description = "Response containing MongoDB connection test results")
public class MongoConnectionTestResponse {

    @Schema(description = "Indicates if the MongoDB connection is successful", example = "true")
    private boolean connected;

    @Schema(description = "MongoDB server version", example = "7.0.0")
    private String serverVersion;

    @Schema(description = "Database name", example = "job-orchestrator")
    private String databaseName;

    @Schema(description = "Message describing the connection status", example = "MongoDB connection successful")
    private String message;

    @Schema(description = "Timestamp when the test was performed", example = "2024-01-01T10:00:00")
    private LocalDateTime testTimestamp;

    public MongoConnectionTestResponse() {
    }

    public MongoConnectionTestResponse(boolean connected, String serverVersion, String databaseName, String message) {
        this.connected = connected;
        this.serverVersion = serverVersion;
        this.databaseName = databaseName;
        this.message = message;
        this.testTimestamp = LocalDateTime.now();
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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

