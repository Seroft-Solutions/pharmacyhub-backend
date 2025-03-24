package com.pharmacyhub.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for login validation result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginValidationResultDTO {
    
    @JsonProperty("status")
    private LoginStatus status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("requiresOtp")
    private boolean requiresOtp;
    
    @JsonProperty("sessionId")
    private UUID sessionId;
    
    /**
     * Enum for login validation status
     */
    public enum LoginStatus {
        OK,
        NEW_DEVICE,
        SUSPICIOUS_LOCATION,
        TOO_MANY_DEVICES,
        OTP_REQUIRED
    }
}
