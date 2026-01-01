package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.DbConnectionTestResponse;
import com.jobmanager.orchestrator.application.service.DbConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for database connection testing operations.
 * Exposes endpoints for testing database connectivity.
 */
@RestController
@RequestMapping("/db")
@Tag(name = "Database Connection", description = "API for testing database connectivity")
public class DbConnectionController {

    private static final Logger logger = LoggerFactory.getLogger(DbConnectionController.class);

    @Autowired
    private DbConnectionService dbConnectionService;

    @GetMapping("/test")
    @Operation(summary = "Test database connection", 
               description = "Tests the database connection by executing a simple SQL query (SELECT CURRENT_DATE FROM SYSIBM.SYSDUMMY1 for DB2)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Database connection test completed"),
        @ApiResponse(responseCode = "500", description = "Internal server error during connection test")
    })
    public ResponseEntity<DbConnectionTestResponse> testConnection() {
        logger.info("Received database connection test request");

        DbConnectionTestResponse response = dbConnectionService.testConnection();
        
        if (response.isConnected()) {
            logger.info("Database connection test successful");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Database connection test failed: {}", response.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

