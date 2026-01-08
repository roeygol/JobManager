package com.jobmanager.orchestrator.persistence.dao;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Data Access Object for MongoDB connection testing.
 * Executes MongoDB commands to test connectivity and retrieve server information.
 */
@Repository
public class MongoConnectionDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionDao.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Tests the MongoDB connection by executing a ping command.
     * Also retrieves server version and database name.
     *
     * @return Map containing connection status, server version, and database name
     * @throws Exception if the MongoDB connection fails
     */
    public Map<String, String> testConnection() throws Exception {
        logger.debug("Testing MongoDB connection...");

        try {
            // Execute ping command to test connection
            Document pingResult = mongoTemplate.getDb().runCommand(new Document("ping", 1));
            logger.debug("MongoDB ping result: {}", pingResult);

            if (pingResult == null || !pingResult.containsKey("ok")) {
                throw new Exception("MongoDB ping command failed: invalid response");
            }

            Object okValue = pingResult.get("ok");
            double ok = okValue instanceof Number ? ((Number) okValue).doubleValue() : 0.0;
            if (ok != 1.0) {
                throw new Exception("MongoDB ping command failed: ok value is " + ok);
            }

            // Get server version
            Document buildInfo = mongoTemplate.getDb().runCommand(new Document("buildInfo", 1));
            String serverVersion = buildInfo != null ? buildInfo.getString("version") : "Unknown";

            // Get database name
            String databaseName = mongoTemplate.getDb().getName();

            logger.info("MongoDB connection test successful. Server version: {}, Database: {}", 
                       serverVersion, databaseName);

            return Map.of(
                "connected", "true",
                "serverVersion", serverVersion != null ? serverVersion : "Unknown",
                "databaseName", databaseName != null ? databaseName : "Unknown"
            );

        } catch (Exception e) {
            logger.error("MongoDB connection test failed", e);
            throw new Exception("MongoDB connection test failed: " + e.getMessage(), e);
        }
    }
}

