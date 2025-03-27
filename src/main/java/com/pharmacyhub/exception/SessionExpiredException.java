package com.pharmacyhub.exception;

import java.util.UUID;

/**
 * Exception for when a session has expired
 */
public class SessionExpiredException extends RuntimeException {
    
    /**
     * Constructs a new session expired exception
     * 
     * @param sessionId The session ID that expired
     */
    public SessionExpiredException(UUID sessionId) {
        super("Session expired: " + sessionId);
    }
    
    /**
     * Constructs a new session expired exception with a custom message
     * 
     * @param message The error message
     */
    public SessionExpiredException(String message) {
        super(message);
    }
}
