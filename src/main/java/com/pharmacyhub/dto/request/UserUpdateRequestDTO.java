package com.pharmacyhub.dto.request;

import com.pharmacyhub.dto.BaseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user information
 * Contains validation rules for user properties that can be updated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDTO implements BaseDTO {
    
    private Long id;
    
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @Email(message = "Email address must be valid")
    private String emailAddress;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Contact number must be valid")
    private String contactNumber;
    
    private boolean openToConnect;
}
