package com.pharmacyhub.exception;

import com.pharmacyhub.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BaseException {
    
    /**
     * Constructs a new exception with the specified resource details
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            ErrorConstants.CODE_NOT_FOUND,
            String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.NOT_FOUND
        );
    }
    
    /**
     * Constructs a new exception with a custom message
     */
    public ResourceNotFoundException(String message) {
        super(
            ErrorConstants.CODE_NOT_FOUND,
            message,
            HttpStatus.NOT_FOUND
        );
    }
}
