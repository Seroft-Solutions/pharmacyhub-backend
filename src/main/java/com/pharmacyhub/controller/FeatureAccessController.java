package com.pharmacyhub.controller;

import com.pharmacyhub.security.domain.Feature;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.dto.AccessProfile;
import com.pharmacyhub.security.dto.FeatureAccessDTO;
import com.pharmacyhub.security.dto.PermissionCheckResponse;
import com.pharmacyhub.security.service.FeatureService;
import com.pharmacyhub.security.service.RBACService;
import com.pharmacyhub.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for feature access-related endpoints
 * Provides centralized access control for frontend components
 */
@RestController
@RequestMapping("/api/feature-access")
@RequiredArgsConstructor
public class FeatureAccessController {
    private final FeatureService featureService;
    private final RBACService rbacService;
    private final SecurityUtils securityUtils;
    
    /**
     * Check if the current user has access to a specific feature
     * Returns detailed access information including allowed operations
     */
    @GetMapping("/check/{featureCode}")
    public ResponseEntity<FeatureAccessDTO> checkFeatureAccess(@PathVariable String featureCode) {
        Long userId = securityUtils.getCurrentUserId();
        Feature feature = featureService.getFeatureByCode(featureCode);
        
        boolean hasAccess = rbacService.userHasFeatureAccess(userId, featureCode);
        
        Set<String> allowedOperations = new HashSet<>();
        if (hasAccess && feature != null) {
            // Check each operation
            for (String operation : feature.getOperations()) {
                if (rbacService.userHasFeatureOperation(userId, featureCode, operation)) {
                    allowedOperations.add(operation);
                }
            }
        }
        
        FeatureAccessDTO accessDTO = new FeatureAccessDTO();
        accessDTO.setFeatureCode(featureCode);
        accessDTO.setName(feature != null ? feature.getName() : featureCode);
        accessDTO.setDescription(feature != null ? feature.getDescription() : "");
        accessDTO.setHasAccess(hasAccess);
        accessDTO.setAllowedOperations(allowedOperations);
        
        return ResponseEntity.ok(accessDTO);
    }
    
    /**
     * Check if the current user can perform a specific operation on a feature
     * Returns true if access is granted, false otherwise
     */
    @GetMapping("/check/{featureCode}/{operation}")
    public ResponseEntity<Boolean> checkOperationAccess(
            @PathVariable String featureCode, 
            @PathVariable String operation) {
        Long userId = securityUtils.getCurrentUserId();
        boolean hasAccess = rbacService.userHasFeatureOperation(userId, featureCode, operation);
        return ResponseEntity.ok(hasAccess);
    }
    
    /**
     * Get all features the current user has access to
     * Returns a list of features with access details
     */
    @GetMapping("/user-features")
    public ResponseEntity<List<FeatureAccessDTO>> getUserFeatures() {
        Long userId = securityUtils.getCurrentUserId();
        List<Feature> features = featureService.getAllFeatures();
        
        List<FeatureAccessDTO> accessDTOs = features.stream()
                .map(feature -> {
                    boolean hasAccess = rbacService.userHasFeatureAccess(userId, feature.getCode());
                    
                    Set<String> allowedOperations = new HashSet<>();
                    if (hasAccess) {
                        // Check each operation
                        for (String operation : feature.getOperations()) {
                            if (rbacService.userHasFeatureOperation(userId, feature.getCode(), operation)) {
                                allowedOperations.add(operation);
                            }
                        }
                    }
                    
                    FeatureAccessDTO dto = new FeatureAccessDTO();
                    dto.setFeatureCode(feature.getCode());
                    dto.setName(feature.getName());
                    dto.setDescription(feature.getDescription());
                    dto.setHasAccess(hasAccess);
                    dto.setAllowedOperations(allowedOperations);
                    return dto;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(accessDTOs);
    }
    
    /**
     * Check if the user has the specified permissions
     */
    @PostMapping("/check-permissions")
    public ResponseEntity<PermissionCheckResponse> checkPermissions(@RequestBody List<String> permissions) {
        Long userId = securityUtils.getCurrentUserId();
        PermissionCheckResponse response = new PermissionCheckResponse();
        
        // Check each permission
        for (String permission : permissions) {
            boolean hasPermission = rbacService.userHasPermission(userId, permission);
            response.addPermission(permission, hasPermission);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if the user has access based on roles and permissions
     */
    @PostMapping("/check-access")
    public ResponseEntity<Boolean> checkAccess(
            @RequestBody Map<String, Object> request) {
        Long userId = securityUtils.getCurrentUserId();
        
        // Extract request parameters
        List<String> roles = (List<String>) request.getOrDefault("roles", new ArrayList<String>());
        List<String> permissions = (List<String>) request.getOrDefault("permissions", new ArrayList<String>());
        boolean requireAll = (boolean) request.getOrDefault("requireAll", true);
        
        boolean hasAccess = false;
        
        // Check roles
        boolean hasRequiredRoles = roles.isEmpty();
        if (!roles.isEmpty()) {
            hasRequiredRoles = requireAll
                    ? roles.stream().allMatch(role -> rbacService.userHasRole(userId, role))
                    : roles.stream().anyMatch(role -> rbacService.userHasRole(userId, role));
        }
        
        // Check permissions
        boolean hasRequiredPermissions = permissions.isEmpty();
        if (!permissions.isEmpty()) {
            hasRequiredPermissions = requireAll
                    ? permissions.stream().allMatch(permission -> rbacService.userHasPermission(userId, permission))
                    : permissions.stream().anyMatch(permission -> rbacService.userHasPermission(userId, permission));
        }
        
        // User must satisfy both role and permission requirements
        hasAccess = hasRequiredRoles && hasRequiredPermissions;
        
        return ResponseEntity.ok(hasAccess);
    }
    
    /**
     * Get the current user's access profile
     */
    @GetMapping("/profile")
    public ResponseEntity<AccessProfile> getUserAccessProfile() {
        Long userId = securityUtils.getCurrentUserId();
        String username = securityUtils.getCurrentUsername();
        
        // Get user's roles and permissions
        Set<String> roles = rbacService.getUserRoles(userId).stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
                
        Set<String> permissions = rbacService.getUserEffectivePermissions(userId).stream()
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());
                
        // Get user's accessible features
        Set<Feature> accessibleFeatures = rbacService.getUserAccessibleFeatures(userId);
        Set<FeatureAccessDTO> featureDTOs = accessibleFeatures.stream()
                .map(feature -> {
                    Set<String> allowedOperations = new HashSet<>();
                    for (String operation : feature.getOperations()) {
                        if (rbacService.userHasFeatureOperation(userId, feature.getCode(), operation)) {
                            allowedOperations.add(operation);
                        }
                    }
                    
                    FeatureAccessDTO dto = new FeatureAccessDTO();
                    dto.setFeatureCode(feature.getCode());
                    dto.setName(feature.getName());
                    dto.setDescription(feature.getDescription());
                    dto.setHasAccess(true);
                    dto.setAllowedOperations(allowedOperations);
                    return dto;
                })
                .collect(Collectors.toSet());
        
        // Create the access profile
        AccessProfile profile = new AccessProfile();
        profile.setUserId(userId);
        profile.setUsername(username);
        profile.setRoles(new ArrayList<>(roles));
        profile.setPermissions(new ArrayList<>(permissions));
        profile.setFeatures(featureDTOs);
        
        return ResponseEntity.ok(profile);
    }
}
