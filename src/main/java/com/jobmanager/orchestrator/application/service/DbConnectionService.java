package com.jobmanager.orchestrator.application.service;

import com.jobmanager.orchestrator.api.dto.DbConnectionTestResponse;
import com.jobmanager.orchestrator.persistence.dao.DbConnectionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for testing database connection.
 * Coordinates database connectivity tests and builds response DTOs.
 */
@Service
public class DbConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(DbConnectionService.class);

    private final DbConnectionDao dbConnectionDao;

    public DbConnectionService(DbConnectionDao dbConnectionDao) {
        this.dbConnectionDao = dbConnectionDao;
    }

    /**
     * Tests the database connection by executing a simple query.
     *
     * @return DbConnectionTestResponse containing connection status and database date
     */
    @Transactional(readOnly = true)
    public DbConnectionTestResponse testConnection() {
        logger.info("Starting database connection test");

        try {
            LocalDateTime currentDate = dbConnectionDao.testConnection();
            
            String successMessage = "Database connection successful. Current date from database: " + currentDate;
            logger.info(successMessage);

            return new DbConnectionTestResponse(true, currentDate, successMessage);

        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            
            String errorMessage = "Database connection failed: " + e.getMessage();
            return new DbConnectionTestResponse(false, null, errorMessage);
        }
    }
}

