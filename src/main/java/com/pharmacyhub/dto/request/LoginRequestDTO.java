package com.pharmacyhub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pharmacyhub.dto.BaseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO with device identification for anti-sharing protection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO implements BaseDTO {
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    private String emailAddress;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    // Anti-sharing protection fields
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("userAgent")
    private String userAgent;
    
    @JsonProperty("platform")
    private String platform;
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("screenWidth")
    private String screenWidth;
    
    @JsonProperty("screenHeight")
    private String screenHeight;
    
    @JsonProperty("colorDepth")
    private String colorDepth;
    
    @JsonProperty("timezone")
    private String timezone;
}
