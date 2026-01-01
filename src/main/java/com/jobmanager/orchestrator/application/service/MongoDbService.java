package com.jobmanager.orchestrator.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmanager.orchestrator.persistence.mongodb.document.MongoDocument;
import com.jobmanager.orchestrator.persistence.mongodb.repository.MongoDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for MongoDB operations.
 * Provides methods to store and retrieve JSON documents from MongoDB.
 */
@Service
public class MongoDbService {

    private static final Logger logger = LoggerFactory.getLogger(MongoDbService.class);

    @Autowired
    private MongoDocumentRepository mongoDocumentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Retrieves a document by its ID.
     *
     * @param id the document ID
     * @return Optional containing the document if found
     */
    public Optional<MongoDocument> getById(String id) {
        logger.debug("Retrieving document by ID: {}", id);
        return mongoDocumentRepository.findById(id);
    }

    /**
     * Retrieves a document by its document key.
     *
     * @param documentKey the document key
     * @return Optional containing the document if found
     */
    public Optional<MongoDocument> getByDocumentKey(String documentKey) {
        logger.debug("Retrieving document by key: {}", documentKey);
        return mongoDocumentRepository.findByDocumentKey(documentKey);
    }

    /**
     * Retrieves the JSON data from a document by its document key.
     *
     * @param documentKey the document key
     * @return Optional containing the data map if document found
     */
    public Optional<Map<String, Object>> getDataByDocumentKey(String documentKey) {
        logger.debug("Retrieving data by document key: {}", documentKey);
        return mongoDocumentRepository.findByDocumentKey(documentKey)
                .map(MongoDocument::getData);
    }

    /**
     * Retrieves a value from a document's data by document key and data key.
     *
     * @param documentKey the document key
     * @param dataKey the key within the data map
     * @return Optional containing the value if found
     */
    public Optional<Object> getValueByDocumentKeyAndDataKey(String documentKey, String dataKey) {
        logger.debug("Retrieving value from document key: {} with data key: {}", documentKey, dataKey);
        return getDataByDocumentKey(documentKey)
                .map(data -> data.get(dataKey));
    }

    /**
     * Retrieves all documents from the database.
     *
     * @return list of all documents
     */
    public List<MongoDocument> getAllDocuments() {
        logger.debug("Retrieving all documents");
        return mongoDocumentRepository.findAll();
    }

    /**
     * Retrieves a document as JSON string.
     *
     * @param documentKey the document key
     * @return Optional containing the JSON string representation if document found
     */
    public Optional<String> getDocumentAsJsonString(String documentKey) {
        logger.debug("Retrieving document as JSON string by key: {}", documentKey);
        return getByDocumentKey(documentKey)
                .map(document -> {
                    try {
                        return objectMapper.writeValueAsString(document);
                    } catch (Exception e) {
                        logger.error("Failed to convert document to JSON string", e);
                        return null;
                    }
                });
    }

    /**
     * Retrieves only the data portion of a document as JSON string.
     *
     * @param documentKey the document key
     * @return Optional containing the JSON string of the data map if document found
     */
    public Optional<String> getDataAsJsonString(String documentKey) {
        logger.debug("Retrieving data as JSON string by key: {}", documentKey);
        return getDataByDocumentKey(documentKey)
                .map(data -> {
                    try {
                        return objectMapper.writeValueAsString(data);
                    } catch (Exception e) {
                        logger.error("Failed to convert data to JSON string", e);
                        return null;
                    }
                });
    }

    /**
     * Checks if a document exists with the given document key.
     *
     * @param documentKey the document key to check
     * @return true if document exists, false otherwise
     */
    public boolean documentExists(String documentKey) {
        logger.debug("Checking if document exists with key: {}", documentKey);
        return mongoDocumentRepository.existsByDocumentKey(documentKey);
    }

    /**
     * Saves or updates a document in MongoDB.
     *
     * @param documentKey the document key
     * @param data the data to store
     * @return the saved document
     */
    public MongoDocument saveDocument(String documentKey, Map<String, Object> data) {
        logger.info("Saving document with key: {}", documentKey);
        
        MongoDocument document = mongoDocumentRepository.findByDocumentKey(documentKey)
                .orElse(new MongoDocument());
        
        document.setDocumentKey(documentKey);
        document.setData(data);
        
        MongoDocument saved = mongoDocumentRepository.save(document);
        logger.info("Document saved with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Converts a Java object to a Map for storage in MongoDB.
     *
     * @param object the object to convert
     * @return Map representation of the object
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToMap(Object object) {
        try {
            return objectMapper.convertValue(object, Map.class);
        } catch (Exception e) {
            logger.error("Failed to convert object to map", e);
            throw new RuntimeException("Failed to convert object to map", e);
        }
    }
}

