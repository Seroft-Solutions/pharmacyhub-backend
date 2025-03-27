package com.pharmacyhub.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails or credentials are invalid
 */
public class UnauthorizedException extends BaseException {
    
    /**
     * Constructs a new exception with the default message from ExceptionConstants
     */
    public UnauthorizedException() {
        super(ExceptionConstants.AUTHENTICATION_FAILED);
    }
    
    /**
     * Constructs a new exception with a custom message but using the constant's code and resolution
     */
    public UnauthorizedException(String message) {
        super(ExceptionConstants.AUTHENTICATION_FAILED, message);
    }
    
    /**
     * Constructs a new exception with the specified constant
     */
    public UnauthorizedException(ExceptionConstants exceptionConstant) {
        super(exceptionConstant);
    }
    
    /**
     * Constructs a new exception with a custom message but using the specified constant's code and resolution
     */
    public UnauthorizedException(ExceptionConstants exceptionConstant, String message) {
        super(exceptionConstant, message);
    }
    
    /**
     * Constructs a new exception with the default constant and a cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(
            ExceptionConstants.AUTHENTICATION_FAILED.getCode(),
            message,
            ExceptionConstants.AUTHENTICATION_FAILED.getResolution(),
            HttpStatus.UNAUTHORIZED,
            cause
        );
    }
}
