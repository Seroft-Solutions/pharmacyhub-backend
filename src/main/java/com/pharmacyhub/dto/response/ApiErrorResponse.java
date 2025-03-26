package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard error response structure for all API errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private int status;
    private String errorCode;
    private String message;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String path;
    
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();
    
    /**
     * Add detail to the error response
     */
    public ApiErrorResponse addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }
    
    /**
     * Create a new error response builder with the current timestamp
     */
    public static Builder builder() {
        return new Builder().timestamp(LocalDateTime.now());
    }
    
    /**
     * Builder implementation for ApiErrorResponse
     */
    public static class Builder {
        private int status;
        private String errorCode;
        private String message;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String path;
        private Map<String, Object> details = new HashMap<>();
        
        public Builder status(int status) {
            this.status = status;
            return this;
        }
        
        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }
        
        public ApiErrorResponse build() {
            return new ApiErrorResponse(status, errorCode, message, timestamp, path, details);
        }
    }
    
    /**
     * Create a simple error response with just status and message
     */
    public static ApiErrorResponse of(int status, String errorCode, String message) {
        return ApiErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
    
    /**
     * Create a simple error response with status, message, and path
     */
    public static ApiErrorResponse of(int status, String errorCode, String message, String path) {
        return ApiErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .build();
    }
}
