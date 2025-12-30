package com.jobmanager.orchestrator.persistence.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Data Access Object for database connection testing.
 * Executes raw SQL queries to test database connectivity.
 */
@Repository
public class DbConnectionDao {

    private static final Logger logger = LoggerFactory.getLogger(DbConnectionDao.class);

    private final JdbcTemplate jdbcTemplate;

    public DbConnectionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Tests the database connection by executing a simple query.
     * For DB2, uses SELECT CURRENT_DATE FROM SYSIBM.SYSDUMMY1
     * Falls back to SELECT CURRENT_DATE if DB2-specific syntax fails.
     *
     * @return the current date from the database
     * @throws Exception if the database connection fails
     */
    public LocalDateTime testConnection() throws Exception {
        logger.debug("Testing database connection...");

        try {
            // Try DB2-specific query first
            String db2Query = "SELECT CURRENT_DATE FROM SYSIBM.SYSDUMMY1";
            logger.debug("Executing DB2 query: {}", db2Query);

            Date dbDate = jdbcTemplate.queryForObject(db2Query, Date.class);
            
            if (dbDate != null) {
                LocalDate localDate = dbDate.toLocalDate();
                LocalDateTime dateTime = localDate.atStartOfDay();
                logger.info("Database connection test successful. Current date from DB: {}", dateTime);
                return dateTime;
            } else {
                throw new Exception("Query returned null result");
            }

        } catch (Exception e) {
            logger.warn("DB2-specific query failed, trying generic query: {}", e.getMessage());
            
            try {
                // Fallback to generic SQL query
                String genericQuery = "SELECT CURRENT_DATE";
                logger.debug("Executing generic query: {}", genericQuery);

                Date dbDate = jdbcTemplate.queryForObject(genericQuery, Date.class);
                
                if (dbDate != null) {
                    LocalDate localDate = dbDate.toLocalDate();
                    LocalDateTime dateTime = localDate.atStartOfDay();
                    logger.info("Database connection test successful with generic query. Current date from DB: {}", dateTime);
                    return dateTime;
                } else {
                    throw new Exception("Query returned null result");
                }

            } catch (Exception fallbackException) {
                logger.error("Both DB2 and generic queries failed", fallbackException);
                throw new Exception("Database connection test failed: " + fallbackException.getMessage(), fallbackException);
            }
        }
    }
}

