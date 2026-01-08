package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.api.dto.MongoConnectionTestResponse;
import com.jobmanager.orchestrator.persistence.dao.MongoConnectionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for testing MongoDB connection.
 * Coordinates MongoDB connectivity tests and builds response DTOs.
 */
@Service
public class MongoConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionService.class);

    @Autowired
    private MongoConnectionDao mongoConnectionDao;

    /**
     * Tests the MongoDB connection by executing a ping command.
     *
     * @return MongoConnectionTestResponse containing connection status, server version, and database name
     */
    public MongoConnectionTestResponse testConnection() {
        logger.info("Starting MongoDB connection test");

        try {
            Map<String, String> connectionInfo = mongoConnectionDao.testConnection();
            
            String serverVersion = connectionInfo.get("serverVersion");
            String databaseName = connectionInfo.get("databaseName");
            
            String successMessage = String.format(
                "MongoDB connection successful. Server version: %s, Database: %s", 
                serverVersion, databaseName);
            logger.info(successMessage);

            return new MongoConnectionTestResponse(
                true, 
                serverVersion, 
                databaseName, 
                successMessage
            );

        } catch (Exception e) {
            logger.error("MongoDB connection test failed", e);
            
            String errorMessage = "MongoDB connection failed: " + e.getMessage();
            return new MongoConnectionTestResponse(
                false, 
                null, 
                null, 
                errorMessage
            );
        }
    }
}

