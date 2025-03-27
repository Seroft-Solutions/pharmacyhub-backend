package com.pharmacyhub.exception;

import com.pharmacyhub.constants.ErrorConstants;
import com.pharmacyhub.constants.SessionErrorConstants;
import com.pharmacyhub.dto.response.ApiErrorResponse;
import com.pharmacyhub.dto.session.LoginValidationResultDTO;
import com.pharmacyhub.dto.session.SessionErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Exception handler for session-related exceptions
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SessionExceptionHandler extends ResponseEntityExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionExceptionHandler.class);
    
    /**
     * Handle SessionValidationException
     */
    @ExceptionHandler(SessionValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleSessionValidationException(
            SessionValidationException ex, HttpServletRequest request) {
        
        LoginValidationResultDTO.LoginStatus status = ex.getStatus();
        String errorCode = SessionErrorConstants.getErrorCode(status.toString());
        String errorMessage = SessionErrorConstants.getErrorMessage(status.toString());
        String action = SessionErrorConstants.getErrorAction(status.toString());
        
        // Create detailed session error response
        SessionErrorResponseDTO sessionError = SessionErrorResponseDTO.builder()
            .status(status.toString())
            .message(errorMessage)
            .action(action)
            .code(errorCode)
            .severity(determineSeverity(status))
            .recoverable(isRecoverable(status))
            .sessionId(ex.getSessionId())
            .requiresOtp(ex.isRequiresOtp())
            .build();
        
        // Map to HTTP status
        HttpStatus httpStatus = mapToHttpStatus(status);
        
        // Create standard API error response
        ApiErrorResponse response = ApiErrorResponse.builder()
            .status(httpStatus.value())
            .errorCode(errorCode != null ? errorCode : "SESSION_ERROR")
            .message(errorMessage != null ? errorMessage : ex.getMessage())
            .path(request.getRequestURI())
            .details(sessionError)
            .build();
        
        logger.warn("Session validation exception: {} - {}", ex.getStatus(), ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }
    
    /**
     * Handle SessionNotFoundException
     */
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSessionNotFoundException(
            SessionNotFoundException ex, HttpServletRequest request) {
        
        ApiErrorResponse response = ApiErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .errorCode(ErrorConstants.CODE_NOT_FOUND)
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        
        logger.warn("Session not found: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle SessionExpiredException
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleSessionExpiredException(
            SessionExpiredException ex, HttpServletRequest request) {
        
        // Create detailed session error response
        SessionErrorResponseDTO sessionError = SessionErrorResponseDTO.builder()
            .status("SESSION_EXPIRED")
            .message(SessionErrorConstants.SESSION_EXPIRED)
            .action(SessionErrorConstants.ACTION_INACTIVE)
            .code(SessionErrorConstants.SESS_007)
            .severity("warning")
            .recoverable(true)
            .build();
        
        ApiErrorResponse response = ApiErrorResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .errorCode("SESSION_EXPIRED")
            .message(SessionErrorConstants.SESSION_EXPIRED)
            .path(request.getRequestURI())
            .details(sessionError)
            .build();
        
        logger.warn("Session expired: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Map login status to HTTP status
     */
    private HttpStatus mapToHttpStatus(LoginValidationResultDTO.LoginStatus status) {
        switch (status) {
            case TOO_MANY_DEVICES:
            case SUSPICIOUS_LOCATION:
            case NEW_DEVICE:
            case OTP_REQUIRED:
                return HttpStatus.UNAUTHORIZED;
            case ACCOUNT_BLOCKED:
                return HttpStatus.FORBIDDEN;
            case OK:
            default:
                return HttpStatus.BAD_REQUEST;
        }
    }
    
    /**
     * Determine severity based on login status
     */
    private String determineSeverity(LoginValidationResultDTO.LoginStatus status) {
        switch (status) {
            case TOO_MANY_DEVICES:
            case SUSPICIOUS_LOCATION:
                return "warning";
            case NEW_DEVICE:
            case OTP_REQUIRED:
                return "info";
            case ACCOUNT_BLOCKED:
                return "error";
            case OK:
            default:
                return "info";
        }
    }
    
    /**
     * Determine if the error is recoverable
     */
    private boolean isRecoverable(LoginValidationResultDTO.LoginStatus status) {
        switch (status) {
            case ACCOUNT_BLOCKED:
                return false;
            case TOO_MANY_DEVICES:
            case SUSPICIOUS_LOCATION:
            case NEW_DEVICE:
            case OTP_REQUIRED:
            case OK:
            default:
                return true;
        }
    }
}
