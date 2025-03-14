package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for permission check response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCheckResponse {
    
    /**
     * Map of permission names to boolean indicating if the user has the permission
     */
    @Builder.Default
    private Map<String, Boolean> permissions = new HashMap<>();
    
    /**
     * Add a permission check result
     */
    public void addPermission(String permission, boolean hasPermission) {
        permissions.put(permission, hasPermission);
    }
    
    /**
     * Check if user has all permissions
     */
    public boolean hasAllPermissions() {
        return permissions.values().stream().allMatch(value -> value);
    }
    
    /**
     * Check if user has any permission
     */
    public boolean hasAnyPermission() {
        return permissions.values().stream().anyMatch(value -> value);
    }
}
