package com.pharmacyhub.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for application-specific exceptions
 * Provides standard fields for error code, error message, and HTTP status
 */
@Getter
public abstract class BaseException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus status;
    
    /**
     * Constructs a new exception with the specified error code, message, and status
     */
    public BaseException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
    
    /**
     * Constructs a new exception with the specified error code, message, status, and cause
     */
    public BaseException(String errorCode, String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }
}
