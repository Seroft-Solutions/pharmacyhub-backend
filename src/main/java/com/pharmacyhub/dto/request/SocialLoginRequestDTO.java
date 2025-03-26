package com.pharmacyhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Social Login requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequestDTO {
    
    @NotBlank(message = "Authorization code is required")
    private String code;
    
    private String callbackUrl;
    
    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private String platform;
    private String language;
    
    // Screen information for device fingerprinting
    private Integer screenWidth;
    private Integer screenHeight;
    private Integer colorDepth;
    private String timezone;
}
