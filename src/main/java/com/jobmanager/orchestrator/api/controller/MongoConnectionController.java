package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.MongoConnectionTestResponse;
import com.jobmanager.orchestrator.application.service.MongoConnectionService;
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
 * REST controller for MongoDB connection testing operations.
 * Exposes endpoints for testing MongoDB connectivity.
 */
@RestController
@RequestMapping("/mongo/connection")
@Tag(name = "MongoDB Connection", description = "API for testing MongoDB connectivity")
public class MongoConnectionController {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionController.class);

    @Autowired
    private MongoConnectionService mongoConnectionService;

    @GetMapping("/test")
    @Operation(summary = "Test MongoDB connection", 
               description = "Tests the MongoDB connection by executing a ping command and retrieving server information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "MongoDB connection test completed"),
        @ApiResponse(responseCode = "500", description = "Internal server error during connection test")
    })
    public ResponseEntity<MongoConnectionTestResponse> testConnection() {
        logger.info("Received MongoDB connection test request");

        MongoConnectionTestResponse response = mongoConnectionService.testConnection();
        
        if (response.isConnected()) {
            logger.info("MongoDB connection test successful");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("MongoDB connection test failed: {}", response.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

