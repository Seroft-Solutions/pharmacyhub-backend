package com.pharmacyhub.exception;

import java.util.UUID;

/**
 * Exception for when a session is not found
 */
public class SessionNotFoundException extends BaseException {
    
    /**
     * Constructs a new session not found exception
     * 
     * @param sessionId The session ID that was not found
     */
    public SessionNotFoundException(UUID sessionId) {
        super(ExceptionConstants.SESSION_NOT_FOUND);
    }
    
    /**
     * Constructs a new session not found exception with a custom message
     * 
     * @param message The error message
     */
    public SessionNotFoundException(String message) {
        super(ExceptionConstants.SESSION_NOT_FOUND, message);
    }
    
    /**
     * Constructs a new session not found exception with a custom message and cause
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public SessionNotFoundException(String message, Throwable cause) {
        super(ExceptionConstants.SESSION_NOT_FOUND, message, cause);
    }
}
