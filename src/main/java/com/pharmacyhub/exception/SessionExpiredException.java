package com.pharmacyhub.exception;

import java.util.UUID;

/**
 * Exception for when a session has expired
 */
public class SessionExpiredException extends BaseException {
    
    /**
     * Constructs a new session expired exception
     * 
     * @param sessionId The session ID that expired
     */
    public SessionExpiredException(UUID sessionId) {
        super(ExceptionConstants.SESSION_EXPIRED);
    }
    
    /**
     * Constructs a new session expired exception with a custom message
     * 
     * @param message The error message
     */
    public SessionExpiredException(String message) {
        super(ExceptionConstants.SESSION_EXPIRED, message);
    }
    
    /**
     * Constructs a new session expired exception with a custom message and cause
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public SessionExpiredException(String message, Throwable cause) {
        super(ExceptionConstants.SESSION_EXPIRED, message, cause);
    }
}
