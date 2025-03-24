package com.pharmacyhub.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA converter to handle PostgreSQL JSONB type conversions
 * Converts between String in Java and JSONB in PostgreSQL
 */
@Converter
public class JsonbConverter implements AttributeConverter<String, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonbConverter.class);
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        
        // Validate that the string is actually JSON
        if (!isValidJson(attribute)) {
            logger.warn("Invalid JSON provided: {}", attribute);
            // Escape the content and wrap it in a JSON object if it's not valid JSON
            return wrapAsJsonObject(attribute);
        }
        
        return attribute;
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return "{}";
        }
        return dbData;
    }
    
    /**
     * Simple validation to check if a string is valid JSON
     * 
     * @param json String to validate
     * @return true if valid JSON
     */
    private boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        
        // Simple validation by checking for braces
        json = json.trim();
        return (json.startsWith("{") && json.endsWith("}")) || 
               (json.startsWith("[") && json.endsWith("]"));
    }
    
    /**
     * Wrap a non-JSON string as a JSON object
     * 
     * @param text Text to wrap
     * @return JSON object with text as a value
     */
    private String wrapAsJsonObject(String text) {
        // Escape any quotes in the text
        text = text.replace("\"", "\\\"");
        return "{\"value\":\"" + text + "\"}";
    }
}
