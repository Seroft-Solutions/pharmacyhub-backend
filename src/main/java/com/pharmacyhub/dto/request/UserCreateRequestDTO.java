package com.pharmacyhub.dto.request;

import com.pharmacyhub.dto.BaseDTO;
import com.pharmacyhub.entity.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new user
 * Contains validation rules for user properties
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDTO implements BaseDTO {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    private String emailAddress;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Contact number must be valid")
    private String contactNumber;
    
    private UserType userType;
    private boolean openToConnect;
}
