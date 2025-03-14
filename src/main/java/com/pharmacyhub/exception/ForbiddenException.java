package com.pharmacyhub.exception;

import com.pharmacyhub.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user attempts to access a resource they don't have permission for
 */
public class ForbiddenException extends BaseException {
    
    /**
     * Constructs a new exception with the default message
     */
    public ForbiddenException() {
        super(
            ErrorConstants.CODE_ACCESS_DENIED,
            ErrorConstants.ACCESS_DENIED,
            HttpStatus.FORBIDDEN
        );
    }
    
    /**
     * Constructs a new exception with the specified message
     */
    public ForbiddenException(String message) {
        super(
            ErrorConstants.CODE_ACCESS_DENIED,
            message,
            HttpStatus.FORBIDDEN
        );
    }
    
    /**
     * Constructs a new exception with the specified message and cause
     */
    public ForbiddenException(String message, Throwable cause) {
        super(
            ErrorConstants.CODE_ACCESS_DENIED,
            message,
            HttpStatus.FORBIDDEN,
            cause
        );
    }
    
    /**
     * Constructs a new exception with details about the resource and required permission
     */
    public ForbiddenException(String resourceName, String permission) {
        super(
            ErrorConstants.CODE_ACCESS_DENIED,
            String.format("Access denied: You don't have %s permission for %s", permission, resourceName),
            HttpStatus.FORBIDDEN
        );
    }
}
