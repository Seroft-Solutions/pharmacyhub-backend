package com.pharmacyhub.security.evaluator;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.service.RBACService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Set;

@Component
public class PHPermissionEvaluator implements PermissionEvaluator {
    private final RBACService rbacService;

    public PHPermissionEvaluator(RBACService rbacService) {
        this.rbacService = rbacService;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if ((auth == null) || (targetDomainObject == null) || !(permission instanceof String)) {
            return false;
        }

        String targetType = targetDomainObject.toString().toUpperCase();
        String permissionString = permission.toString();

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = (User) userDetails;

        Set<Permission> effectivePermissions = rbacService.getUserEffectivePermissions(user.getId());

        return effectivePermissions.stream()
            .anyMatch(p -> p.getResourceType().name().equals(targetType) &&
                         p.getOperationType().name().equals(permissionString));
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if ((auth == null) || (targetType == null) || !(permission instanceof String)) {
            return false;
        }

        return hasPermission(auth, targetType, permission);
    }
}