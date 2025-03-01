package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.dto.BaseDTO;
import com.pharmacyhub.entity.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for authenticated user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO implements BaseDTO {
    
    private Long id;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private boolean openToConnect;
    private boolean registered;
    private UserType userType;
    private String jwtToken;
    
    @Builder.Default
    private List<String> roles = new ArrayList<>();
    
    @Builder.Default
    private List<String> permissions = new ArrayList<>();
}
