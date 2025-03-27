package com.pharmacyhub.exception;

import java.util.UUID;

/**
 * Exception thrown when a conflicting session is detected.
 * This typically happens when a user attempts to log in from multiple devices.
 */
public class SessionConflictException extends BaseException {
    
    private final UUID existingSessionId;
    private final String ipAddress;
    private final String userAgent;
    
    /**
     * Constructs a new session conflict exception
     * 
     * @param existingSessionId The ID of the existing session
     * @param ipAddress The IP address of the existing session
     * @param userAgent The user agent of the existing session
     */
    public SessionConflictException(UUID existingSessionId, String ipAddress, String userAgent) {
        super(ExceptionConstants.SESSION_CONFLICT);
        this.existingSessionId = existingSessionId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    /**
     * Constructs a new session conflict exception with a custom message
     * 
     * @param message The error message
     */
    public SessionConflictException(String message) {
        super(ExceptionConstants.SESSION_CONFLICT, message);
        this.existingSessionId = null;
        this.ipAddress = null;
        this.userAgent = null;
    }
    
    /**
     * Gets the ID of the existing session
     * 
     * @return The existing session ID
     */
    public UUID getExistingSessionId() {
        return existingSessionId;
    }
    
    /**
     * Gets the IP address of the existing session
     * 
     * @return The IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * Gets the user agent of the existing session
     * 
     * @return The user agent
     */
    public String getUserAgent() {
        return userAgent;
    }
}
