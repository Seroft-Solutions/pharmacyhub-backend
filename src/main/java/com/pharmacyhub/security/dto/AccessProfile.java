package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO containing a user's access profile
 * Includes roles, permissions, and features the user has access to
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessProfile {
    /**
     * User ID
     */
    private Long userId;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * Roles assigned to the user
     */
    @Builder.Default
    private List<String> roles = new ArrayList<>();
    
    /**
     * Permissions granted to the user
     */
    @Builder.Default
    private List<String> permissions = new ArrayList<>();
    
    /**
     * Features the user has access to
     */
    @Builder.Default
    private Set<FeatureAccessDTO> features = new HashSet<>();
}
