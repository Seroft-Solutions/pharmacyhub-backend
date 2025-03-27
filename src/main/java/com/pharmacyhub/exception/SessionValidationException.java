package com.pharmacyhub.exception;

import com.pharmacyhub.dto.session.LoginValidationResultDTO.LoginStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Exception for session validation failures
 */
@Getter
public class SessionValidationException extends BaseException {
    
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
        super(
            ExceptionConstants.SESSION_VALIDATION_ERROR.getCode(),
            message,
            ExceptionConstants.SESSION_VALIDATION_ERROR.getResolution(),
            HttpStatus.BAD_REQUEST
        );
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
    
    /**
     * Constructs a new session validation exception with default values
     * 
     * @param status Login status
     */
    public SessionValidationException(LoginStatus status) {
        this(status, ExceptionConstants.SESSION_VALIDATION_ERROR.getMessage(), null, false);
    }
}
