package com.pharmacyhub.dto;

import com.pharmacyhub.entity.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Data Transfer Object for user profile information
 * Includes comprehensive user data with roles and permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private UserType userType;
    private boolean registered;
    private boolean openToConnect;
    private boolean verified;
    private Set<String> roles;
    private Set<String> permissions;
}