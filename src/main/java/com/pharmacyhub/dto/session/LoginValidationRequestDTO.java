package com.pharmacyhub.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for validating login attempts with device information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginValidationRequestDTO {
    
    @NotBlank(message = "User ID is required")
    @JsonProperty("userId")
    private Long userId;
    
    @NotBlank(message = "Device ID is required")
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("userAgent")
    private String userAgent;
    
    @JsonProperty("platform")
    private String platform;
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("metadata")
    private String metadata;
}
