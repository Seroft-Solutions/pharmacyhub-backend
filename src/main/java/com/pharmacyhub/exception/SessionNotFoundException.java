package com.pharmacyhub.exception;

import java.util.UUID;

/**
 * Exception for when a session is not found
 */
public class SessionNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new session not found exception
     * 
     * @param sessionId The session ID that was not found
     */
    public SessionNotFoundException(UUID sessionId) {
        super("Session not found: " + sessionId);
    }
    
    /**
     * Constructs a new session not found exception with a custom message
     * 
     * @param message The error message
     */
    public SessionNotFoundException(String message) {
        super(message);
    }
}
