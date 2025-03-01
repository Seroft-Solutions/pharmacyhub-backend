package com.pharmacyhub.dto.request;

import com.pharmacyhub.dto.BaseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO
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
}
