package com.jobmanager.orchestrator.api.controller;

import com.jobmanager.orchestrator.api.dto.DeleteDocumentResponse;
import com.jobmanager.orchestrator.api.dto.DocumentResponse;
import com.jobmanager.orchestrator.api.dto.SaveDocumentRequest;
import com.jobmanager.orchestrator.application.service.MongoDbService;
import com.jobmanager.orchestrator.persistence.mongodb.document.MongoDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for MongoDB document operations.
 * Provides endpoints to store, retrieve, and manage JSON documents in MongoDB.
 */
@RestController
@RequestMapping("/mongo/documents")
@Tag(name = "MongoDB Documents", description = "API for managing JSON documents in MongoDB")
@Validated
public class MongoDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(MongoDocumentController.class);

    @Autowired
    private MongoDbService mongoDbService;

    /**
     * Saves or updates a document in MongoDB.
     *
     * @param request the document save request
     * @return the saved document
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Save or update a document", 
               description = "Saves a new document or updates an existing one by document key. " +
                           "If a document with the same key exists, it will be updated.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document saved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<DocumentResponse> saveDocument(@Valid @RequestBody SaveDocumentRequest request) {
        logger.info("Received save document request for key: {}", request.getDocumentKey());

        MongoDocument saved = mongoDbService.saveDocument(request.getDocumentKey(), request.getData());
        DocumentResponse response = toDocumentResponse(saved);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a document by its document key.
     *
     * @param documentKey the document key
     * @return the document if found
     */
    @GetMapping(value = "/{documentKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get document by key", 
               description = "Retrieves a document from MongoDB by its document key")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<DocumentResponse> getDocumentByKey(
            @Parameter(description = "Document key", required = true)
            @PathVariable @NotBlank(message = "Document key is required") String documentKey) {
        
        logger.debug("Retrieving document with key: {}", documentKey);

        Optional<MongoDocument> document = mongoDbService.getByDocumentKey(documentKey);
        
        if (document.isPresent()) {
            return ResponseEntity.ok(toDocumentResponse(document.get()));
        } else {
            logger.warn("Document not found with key: {}", documentKey);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves only the data portion of a document as JSON.
     *
     * @param documentKey the document key
     * @return the document data as JSON
     */
    @GetMapping(value = "/{documentKey}/data", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get document data as JSON", 
               description = "Retrieves only the data portion of a document as JSON string")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<Map<String, Object>> getDocumentData(
            @Parameter(description = "Document key", required = true)
            @PathVariable @NotBlank(message = "Document key is required") String documentKey) {
        
        logger.debug("Retrieving data for document key: {}", documentKey);

        Optional<Map<String, Object>> data = mongoDbService.getDataByDocumentKey(documentKey);
        
        if (data.isPresent()) {
            return ResponseEntity.ok(data.get());
        } else {
            logger.warn("Document not found with key: {}", documentKey);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves a specific value from a document's data.
     *
     * @param documentKey the document key
     * @param dataKey the key within the data map
     * @return the value if found
     */
    @GetMapping(value = "/{documentKey}/value/{dataKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get specific value from document", 
               description = "Retrieves a specific value from a document's data map by data key")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Value retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Document or value not found")
    })
    public ResponseEntity<Object> getDocumentValue(
            @Parameter(description = "Document key", required = true)
            @PathVariable @NotBlank(message = "Document key is required") String documentKey,
            @Parameter(description = "Data key", required = true)
            @PathVariable @NotBlank(message = "Data key is required") String dataKey) {
        
        logger.debug("Retrieving value for document key: {} and data key: {}", documentKey, dataKey);

        Optional<Object> value = mongoDbService.getValueByDocumentKeyAndDataKey(documentKey, dataKey);
        
        if (value.isPresent()) {
            return ResponseEntity.ok(value.get());
        } else {
            logger.warn("Value not found for document key: {} and data key: {}", documentKey, dataKey);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves all documents from MongoDB.
     *
     * @return list of all documents
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all documents", 
               description = "Retrieves all documents from MongoDB")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully")
    })
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        logger.debug("Retrieving all documents");

        List<DocumentResponse> documents = mongoDbService.getAllDocuments().stream()
                .map(this::toDocumentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documents);
    }

    /**
     * Checks if a document exists by its document key.
     *
     * @param documentKey the document key
     * @return true if document exists, false otherwise
     */
    @GetMapping(value = "/{documentKey}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check if document exists", 
               description = "Checks if a document exists in MongoDB by its document key")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Existence check completed")
    })
    public ResponseEntity<Map<String, Object>> documentExists(
            @Parameter(description = "Document key", required = true)
            @PathVariable @NotBlank(message = "Document key is required") String documentKey) {
        
        logger.debug("Checking existence of document with key: {}", documentKey);

        boolean exists = mongoDbService.documentExists(documentKey);
        Map<String, Object> response = Map.of(
                "documentKey", documentKey,
                "exists", exists
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a document by its document key.
     *
     * @param documentKey the document key
     * @return deletion result
     */
    @DeleteMapping(value = "/{documentKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete document by key", 
               description = "Deletes a document from MongoDB by its document key")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document deletion processed"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<DeleteDocumentResponse> deleteDocument(
            @Parameter(description = "Document key", required = true)
            @PathVariable @NotBlank(message = "Document key is required") String documentKey) {
        
        logger.info("Deleting document with key: {}", documentKey);

        boolean deleted = mongoDbService.deleteDocument(documentKey);
        
        if (deleted) {
            DeleteDocumentResponse response = new DeleteDocumentResponse(
                    true, "Document deleted successfully", documentKey);
            return ResponseEntity.ok(response);
        } else {
            DeleteDocumentResponse response = new DeleteDocumentResponse(
                    false, "Document not found", documentKey);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Converts MongoDocument entity to DocumentResponse DTO.
     */
    private DocumentResponse toDocumentResponse(MongoDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getDocumentKey(),
                document.getData(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}

