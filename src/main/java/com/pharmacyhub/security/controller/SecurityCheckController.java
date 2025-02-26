package com.pharmacyhub.security.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.dto.AccessCheckRequest;
import com.pharmacyhub.security.service.RBACService;
import com.pharmacyhub.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for security-related checks
 * Provides endpoints for the frontend to validate permissions and roles
 */
@RestController
@RequestMapping(APIConstants.BASE_MAPPING)
public class SecurityCheckController {

    @Autowired
    private RBACService rbacService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Check multiple permissions at once
     * Returns a map of permission names to boolean values indicating if the user has each permission
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(
            value = APIConstants.API_VERSION_V1 + "/security/check-permissions",
            method = RequestMethod.POST
    )
    public ResponseEntity<Map<String, Boolean>> checkPermissions(@RequestBody List<String> permissions) {
        User currentUser = userService.getLoggedInUser();
        Set<Permission> userPermissions = rbacService.getUserEffectivePermissions(currentUser.getId());
        
        Map<String, Boolean> results = new HashMap<>();
        permissions.forEach(permission -> {
            boolean hasPermission = userPermissions.stream()
                .anyMatch(p -> p.getName().equals(permission));
            results.put(permission, hasPermission);
        });
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Check if the user has access based on roles and permissions
     * Supports checking for multiple roles/permissions with AND/OR logic
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(
            value = APIConstants.API_VERSION_V1 + "/security/check-access",
            method = RequestMethod.POST
    )
    public ResponseEntity<AccessCheckResult> checkAccess(@RequestBody AccessCheckRequest request) {
        User currentUser = userService.getLoggedInUser();
        Set<Permission> userPermissions = rbacService.getUserEffectivePermissions(currentUser.getId());
        Set<Role> userRoles = rbacService.getUserRoles(currentUser.getId());
        
        // Check roles
        boolean hasRole = request.getRoles().isEmpty() || 
            request.getRoles().stream().anyMatch(role -> 
                userRoles.stream().anyMatch(r -> r.getName().equals(role))
            );
            
        // Check permissions
        boolean hasPermission = request.getPermissions().isEmpty() ||
            request.getPermissions().stream().allMatch(permission ->
                userPermissions.stream().anyMatch(p -> p.getName().equals(permission))
            );
        
        boolean hasAccess = request.isRequireAll() 
            ? (hasRole && hasPermission) 
            : (hasRole || hasPermission);
            
        return ResponseEntity.ok(new AccessCheckResult(hasAccess));
    }
    
    /**
     * Get all available permissions in the system (admin only)
     * Useful for permission management in the UI
     */
    @PreAuthorize("hasRole('ADMIN') or hasPermission('PERMISSION', 'MANAGE')")
    @RequestMapping(
            value = APIConstants.API_VERSION_V1 + "/security/available-permissions",
            method = RequestMethod.POST
    )
    public ResponseEntity<List<PermissionInfoDTO>> getAvailablePermissions() {
        List<Permission> allPermissions = rbacService.getAllPermissions();
        
        List<PermissionInfoDTO> permissionDTOs = allPermissions.stream()
            .map(permission -> new PermissionInfoDTO(
                permission.getName(),
                permission.getDescription(),
                permission.getResourceType().name(),
                permission.getOperationType().name(),
                permission.isRequiresApproval()
            ))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(permissionDTOs);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class AccessCheckResult {
        private boolean hasAccess;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class PermissionInfoDTO {
        private String name;
        private String description;
        private String resourceType;
        private String operationType;
        private boolean requiresApproval;
    }
}