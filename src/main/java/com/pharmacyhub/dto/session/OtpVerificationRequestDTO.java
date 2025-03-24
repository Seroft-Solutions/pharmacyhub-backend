package com.pharmacyhub.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OTP verification requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequestDTO {
    
    @NotBlank(message = "OTP code is required")
    @JsonProperty("otp")
    private String otp;
    
    @NotBlank(message = "Device ID is required")
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("userAgent")
    private String userAgent;
    
    @JsonProperty("sessionId")
    private String sessionId;
}
