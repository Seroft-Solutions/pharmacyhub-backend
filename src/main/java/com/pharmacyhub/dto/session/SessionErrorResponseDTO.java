package com.pharmacyhub.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for session error responses with structured information
 * Designed to provide frontend with detailed error handling data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionErrorResponseDTO {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("severity")
    private String severity;
    
    @JsonProperty("recoverable")
    private boolean recoverable;
    
    @JsonProperty("sessionId")
    private UUID sessionId;
    
    @JsonProperty("requiresOtp")
    private boolean requiresOtp;
    
    /**
     * Create error response from login validation result
     * 
     * @param result The login validation result
     * @param errorCode Error code
     * @param errorMessage Error message
     * @param action Suggested action
     * @return New error response DTO
     */
    public static SessionErrorResponseDTO fromLoginValidationResult(
            LoginValidationResultDTO result,
            String errorCode,
            String errorMessage,
            String action) {
        
        // Determine severity based on login status
        String severity = determineSeverity(result.getStatus());
        
        // Determine if the error is recoverable
        boolean recoverable = isRecoverable(result.getStatus());
        
        return SessionErrorResponseDTO.builder()
            .status(result.getStatus().toString())
            .message(errorMessage != null ? errorMessage : result.getMessage())
            .action(action)
            .code(errorCode)
            .severity(severity)
            .recoverable(recoverable)
            .sessionId(result.getSessionId())
            .requiresOtp(result.isRequiresOtp())
            .build();
    }
    
    /**
     * Determine severity based on login status
     * 
     * @param status Login status
     * @return Severity level (info, warning, error, critical)
     */
    private static String determineSeverity(LoginValidationResultDTO.LoginStatus status) {
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
     * 
     * @param status Login status
     * @return True if recoverable
     */
    private static boolean isRecoverable(LoginValidationResultDTO.LoginStatus status) {
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
