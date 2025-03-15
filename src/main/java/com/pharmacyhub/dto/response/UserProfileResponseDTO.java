package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for user profile information
 * Contains basic user information returned by the /users/me endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponseDTO implements BaseDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private boolean active;
    private boolean registered;
    private String avatarUrl;
    private String phoneNumber;
    private Object customData; // For any user-specific data that doesn't fit in the standard fields
}
