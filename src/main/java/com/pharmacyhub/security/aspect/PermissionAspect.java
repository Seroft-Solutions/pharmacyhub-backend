package com.pharmacyhub.security.aspect;

import com.pharmacyhub.engine.PHEngine;
import com.pharmacyhub.security.annotation.RequiresPermission;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.service.RBACService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect extends PHEngine
{
    private final RBACService rbacService;

    @Around("@annotation(com.pharmacyhub.security.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable
    {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);

        // Get current user's permissions
        Set<Permission> userPermissions = rbacService.getUserEffectivePermissions(getLoggedInUser().getId());

        // Check if user has required permission
        boolean hasPermission;
        
        // If a specific permission name is provided, check for that
        if (annotation.permissionName() != null && !annotation.permissionName().isEmpty()) {
            hasPermission = userPermissions.stream()
                                         .anyMatch(permission -> permission.getName().equals(annotation.permissionName()));
        } else {
            // Otherwise check based on resource and operation
            hasPermission = userPermissions.stream()
                                         .anyMatch(permission -> permission.getResourceType() == annotation.resource() &&
                                                 permission.getOperationType() == annotation.operation() &&
                                                 (!annotation.requiresApproval() ||
                                                         permission.isRequiresApproval()));
        }

        if (!hasPermission)
        {
            if (annotation.permissionName() != null && !annotation.permissionName().isEmpty()) {
                throw new AccessDeniedException(
                        "User does not have required permission: " + annotation.permissionName());
            } else {
                throw new AccessDeniedException(
                        "User does not have required permission: " + annotation.resource() + ":" + annotation.operation());
            }
        }

        return joinPoint.proceed();
    }
}
