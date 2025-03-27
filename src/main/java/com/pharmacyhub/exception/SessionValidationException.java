package com.pharmacyhub.exception;

import com.pharmacyhub.dto.session.LoginValidationResultDTO.LoginStatus;
import lombok.Getter;

import java.util.UUID;

/**
 * Exception for session validation failures
 */
@Getter
public class SessionValidationException extends RuntimeException {
    
    private final LoginStatus status;
    private final UUID sessionId;
    private final boolean requiresOtp;
    
    /**
     * Constructs a new session validation exception
     * 
     * @param status Login status
     * @param message The error message
     * @param sessionId Optional session ID
     * @param requiresOtp Whether OTP is required
     */
    public SessionValidationException(LoginStatus status, String message, UUID sessionId, boolean requiresOtp) {
        super(message);
        this.status = status;
        this.sessionId = sessionId;
        this.requiresOtp = requiresOtp;
    }
    
    /**
     * Constructs a new session validation exception without session ID
     * 
     * @param status Login status
     * @param message The error message
     */
    public SessionValidationException(LoginStatus status, String message) {
        this(status, message, null, false);
    }
}
