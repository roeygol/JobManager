package com.jobmanager.orchestrator.persistence.mongodb.repository;

import com.jobmanager.orchestrator.persistence.mongodb.document.MongoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for MongoDocument entities.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface MongoDocumentRepository extends MongoRepository<MongoDocument, String> {

    /**
     * Finds a document by its document key.
     *
     * @param documentKey the document key to search for
     * @return Optional containing the document if found
     */
    Optional<MongoDocument> findByDocumentKey(String documentKey);

    /**
     * Checks if a document exists with the given document key.
     *
     * @param documentKey the document key to check
     * @return true if document exists, false otherwise
     */
    boolean existsByDocumentKey(String documentKey);
}

