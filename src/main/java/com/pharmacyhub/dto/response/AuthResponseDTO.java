package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pharmacyhub.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for authenticated user information that matches frontend's AuthResponse structure
 * Enhanced with session validation for anti-sharing protection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO implements BaseDTO {
    private UserResponseDTO user;
    private TokensDTO tokens;
    
    // Anti-sharing protection fields
    @JsonProperty("sessionId")
    private UUID sessionId;
    
    @JsonProperty("validationStatus")
    private String validationStatus;
}
