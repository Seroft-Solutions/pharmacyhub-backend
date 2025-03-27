package com.pharmacyhub.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for application-specific exceptions
 * Provides standard fields for error code, error message, resolution, and HTTP status
 */
@Getter
public abstract class BaseException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus status;
    private final String resolution;
    
    /**
     * Constructs a new exception with the specified error code, message, resolution, and status
     */
    public BaseException(String errorCode, String message, String resolution, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.resolution = resolution;
        this.status = status;
    }
    
    /**
     * Constructs a new exception with the specified error code, message, resolution, status, and cause
     */
    public BaseException(String errorCode, String message, String resolution, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.resolution = resolution;
        this.status = status;
    }
    
    /**
     * Constructs a new exception from an ExceptionConstants enum value
     */
    public BaseException(ExceptionConstants exceptionConstant) {
        super(exceptionConstant.getMessage());
        this.errorCode = exceptionConstant.getCode();
        this.resolution = exceptionConstant.getResolution();
        this.status = exceptionConstant.getStatus();
    }
    
    /**
     * Constructs a new exception from an ExceptionConstants enum value with a custom message
     */
    public BaseException(ExceptionConstants exceptionConstant, String customMessage) {
        super(customMessage);
        this.errorCode = exceptionConstant.getCode();
        this.resolution = exceptionConstant.getResolution();
        this.status = exceptionConstant.getStatus();
    }
    
    /**
     * Constructs a new exception from an ExceptionConstants enum value with a cause
     */
    public BaseException(ExceptionConstants exceptionConstant, Throwable cause) {
        super(exceptionConstant.getMessage(), cause);
        this.errorCode = exceptionConstant.getCode();
        this.resolution = exceptionConstant.getResolution();
        this.status = exceptionConstant.getStatus();
    }
}
