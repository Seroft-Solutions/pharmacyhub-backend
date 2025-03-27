package com.pharmacyhub.exception;

import com.pharmacyhub.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails or credentials are invalid
 */
public class UnauthorizedException extends BaseException {
    
    /**
     * Constructs a new exception with the default message
     */
    public UnauthorizedException() {
        super(
            ErrorConstants.CODE_AUTHENTICATION,
            ErrorConstants.AUTHENTICATION_FAILED,
            HttpStatus.UNAUTHORIZED
        );
    }
    
    /**
     * Constructs a new exception with the specified message
     */
    public UnauthorizedException(String message) {
        super(
            ErrorConstants.CODE_AUTHENTICATION,
            message,
            HttpStatus.UNAUTHORIZED
        );
    }
    
    /**
     * Constructs a new exception with the specified message and cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(
            ErrorConstants.CODE_AUTHENTICATION,
            message,
            HttpStatus.UNAUTHORIZED,
            cause
        );
    }
}
