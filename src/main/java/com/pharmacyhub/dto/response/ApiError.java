package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard error response object
 * Used within ApiResponse to provide consistent error information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private int status;
    private String message;
    private Map<String, Object> details;
    
    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }
    
    /**
     * Add details to the error
     */
    public ApiError addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }
}
