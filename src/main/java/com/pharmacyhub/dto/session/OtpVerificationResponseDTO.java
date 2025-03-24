package com.pharmacyhub.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for OTP verification responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationResponseDTO {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("sessionId")
    private UUID sessionId;
}
