package com.pharmacyhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password reset requests
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequestDTO {
    
    private String emailAddress;
    
    // Device information
    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private String platform;
    private String language;
    
    // Additional device attributes
    private Integer screenWidth;
    private Integer screenHeight;
    private Integer colorDepth;
    private String timezone;
}
