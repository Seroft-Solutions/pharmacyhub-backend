package com.pharmacyhub.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO for login sessions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginSessionDTO {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("userAgent")
    private String userAgent;
    
    @JsonProperty("loginTime")
    private ZonedDateTime loginTime;
    
    @JsonProperty("lastActive")
    private ZonedDateTime lastActive;
    
    @JsonProperty("active")
    private boolean active;
    
    @JsonProperty("requiresOtp")
    private boolean requiresOtp;
    
    @JsonProperty("otpVerified")
    private boolean otpVerified;
    
    @JsonProperty("metadata")
    private String metadata;
}
