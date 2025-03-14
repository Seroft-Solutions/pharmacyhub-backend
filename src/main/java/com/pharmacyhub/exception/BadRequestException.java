package com.pharmacyhub.exception;

import com.pharmacyhub.constants.ErrorConstants;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown for invalid input parameters or request validation failures
 */
public class BadRequestException extends BaseException {
    
    /**
     * Constructs a new exception with the specified message
     */
    public BadRequestException(String message) {
        super(
            ErrorConstants.CODE_VALIDATION,
            message,
            HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * Constructs a new exception with the specified message and cause
     */
    public BadRequestException(String message, Throwable cause) {
        super(
            ErrorConstants.CODE_VALIDATION,
            message,
            HttpStatus.BAD_REQUEST,
            cause
        );
    }
}
