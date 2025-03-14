package com.pharmacyhub.exception;

import com.pharmacyhub.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is a conflict with the current state of the resource
 * Typically used for duplicate entries or concurrent modification conflicts
 */
public class ConflictException extends BaseException {
    
    /**
     * Constructs a new exception with the specified message
     */
    public ConflictException(String message) {
        super(
            ErrorConstants.CODE_DUPLICATE,
            message,
            HttpStatus.CONFLICT
        );
    }
    
    /**
     * Constructs a new exception with the specified resource details
     */
    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(
            ErrorConstants.CODE_DUPLICATE,
            String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.CONFLICT
        );
    }
    
    /**
     * Constructs a new exception with the specified message and cause
     */
    public ConflictException(String message, Throwable cause) {
        super(
            ErrorConstants.CODE_DUPLICATE,
            message,
            HttpStatus.CONFLICT,
            cause
        );
    }
}
