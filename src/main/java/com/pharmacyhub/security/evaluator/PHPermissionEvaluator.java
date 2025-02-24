package com.pharmacyhub.security.evaluator;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.service.AuditService;
import com.pharmacyhub.security.service.RBACService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Set;

@Component
@Slf4j
public class PHPermissionEvaluator implements PermissionEvaluator {

    private final RBACService rbacService;
    private final AuditService auditService;

    public PHPermissionEvaluator(RBACService rbacService, AuditService auditService) {
        this.rbacService = rbacService;
        this.auditService = auditService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if ((authentication == null) || !(permission instanceof String)) {
            return false;
        }

        String permissionString = permission.toString();
        
        // If no target is specified, treat it as a generic permission check
        if (targetDomainObject == null) {
            return hasGenericPermission(authentication, permissionString);
        }

        // Get the resource type from the target domain object
        String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();
        
        // Get the user from the authentication
        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            return false;
        }
        
        User user = (User) authentication.getPrincipal();
        
        // Get the user's effective permissions
        Set<Permission> effectivePermissions = rbacService.getUserEffectivePermissions(user.getId());
        
        // Check if the user has the required permission for the target
        boolean hasPermission = effectivePermissions.stream()
                .anyMatch(p -> matchesPermission(p, targetType, permissionString));
        
        // Log the permission check
        auditService.logSecurityEvent(
            "PERMISSION_CHECK",
            String.format("User %s checked permission %s on %s", 
                user.getUsername(), permissionString, targetType),
            hasPermission ? "GRANTED" : "DENIED"
        );
        
        return hasPermission;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if ((authentication == null) || (targetType == null) || !(permission instanceof String)) {
            return false;
        }
        
        // When using this method, the targetType is a string specifying the type
        String permissionString = permission.toString();
        
        // Get the user from the authentication
        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            return false;
        }
        
        User user = (User) authentication.getPrincipal();
        
        // Get the user's effective permissions
        Set<Permission> effectivePermissions = rbacService.getUserEffectivePermissions(user.getId());
        
        // Check if the user has the required permission for the target type
        boolean hasPermission = effectivePermissions.stream()
                .anyMatch(p -> matchesPermission(p, targetType, permissionString));
        
        // Log the permission check with target ID
        auditService.logSecurityEvent(
            "PERMISSION_CHECK",
            String.format("User %s checked permission %s on %s with ID %s", 
                user.getUsername(), permissionString, targetType, targetId),
            hasPermission ? "GRANTED" : "DENIED"
        );
        
        return hasPermission;
    }
    
    /**
     * Checks if the user has a generic permission that doesn't target a specific resource.
     */
    private boolean hasGenericPermission(Authentication authentication, String permissionString) {
        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            return false;
        }
        
        User user = (User) authentication.getPrincipal();
        Set<Permission> effectivePermissions = rbacService.getUserEffectivePermissions(user.getId());
        
        // For generic permissions, match by name
        boolean hasPermission = effectivePermissions.stream()
                .anyMatch(p -> p.getName().equals(permissionString));
        
        // Log the permission check
        auditService.logSecurityEvent(
            "PERMISSION_CHECK",
            String.format("User %s checked generic permission %s", 
                user.getUsername(), permissionString),
            hasPermission ? "GRANTED" : "DENIED"
        );
        
        return hasPermission;
    }
    
    /**
     * Checks if a permission matches the target type and permission string.
     */
    private boolean matchesPermission(Permission permission, String targetType, String permissionString) {
        // Check for exact match
        if (permission.getName().equals(permissionString)) {
            return true;
        }
        
        // Check for type + operation match
        try {
            ResourceType resourceType = ResourceType.valueOf(targetType);
            return permission.getResourceType() == resourceType && 
                   permission.getOperationType().name().equals(permissionString);
        } catch (IllegalArgumentException e) {
            // If the target type doesn't match a ResourceType, fall back to name matching
            return false;
        }
    }
}
