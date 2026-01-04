package com.jobmanager.orchestrator.persistence.mongodb.document;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * MongoDB document entity for storing JSON data.
 * This is a generic document that can store any JSON-structured data.
 */
@Document(collection = "documents")
public class MongoDocument extends BaseMongoDocument {

    private String documentKey;
    private Map<String, Object> data;

    public MongoDocument() {
        super();
    }

    public MongoDocument(String documentKey, Map<String, Object> data) {
        super();
        this.documentKey = documentKey;
        this.data = data;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public void setDocumentKey(String documentKey) {
        this.documentKey = documentKey;
        touch();
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
        touch();
    }
}

