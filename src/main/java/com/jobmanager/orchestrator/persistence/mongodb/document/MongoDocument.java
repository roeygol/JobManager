package com.jobmanager.orchestrator.persistence.mongodb.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * MongoDB document entity for storing generic JSON documents.
 * Provides a flexible document storage mechanism with a unique document key.
 * 
 * Structure:
 * - Key: documentKey (unique identifier)
 * - Fields: data (Map<String, Object> for flexible JSON storage)
 */
@Document(collection = "mongo_documents")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MongoDocument extends BaseMongoDocument {

    @Indexed(unique = true)
    @JsonProperty("documentKey")
    private String documentKey;

    @JsonProperty("data")
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
