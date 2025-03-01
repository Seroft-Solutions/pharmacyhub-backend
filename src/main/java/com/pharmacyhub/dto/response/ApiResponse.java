package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard API response wrapper for all endpoints
 * Provides consistent structure for all API responses
 *
 * @param <T> Type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T data;
    private ApiError error;
    private int status;
    private LocalDateTime timestamp;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Creates a success response with the provided data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates a success response with the provided data and status code
     */
    public static <T> ApiResponse<T> success(T data, int status) {
        return ApiResponse.<T>builder()
                .data(data)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates a success response with the provided data, status code, and metadata
     */
    public static <T> ApiResponse<T> success(T data, int status, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .data(data)
                .status(status)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates an error response with the provided status and message
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .error(new ApiError(status, message))
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates an error response with the provided status, message, and additional details
     */
    public static <T> ApiResponse<T> error(int status, String message, Map<String, Object> details) {
        ApiError error = new ApiError(status, message);
        error.setDetails(details);
        
        return ApiResponse.<T>builder()
                .error(error)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Add metadata to the response
     */
    public ApiResponse<T> addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
}
