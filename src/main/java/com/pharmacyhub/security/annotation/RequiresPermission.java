package com.pharmacyhub.security.annotation;

import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.ResourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify required permissions for accessing methods.
 * Used in conjunction with PermissionAspect for RBAC enforcement.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    /**
     * The resource type being accessed
     */
    ResourceType resource();
    
    /**
     * The operation being performed on the resource
     */
    OperationType operation();
    
    /**
     * The permission name (optional)
     * If specified, this exact permission will be checked instead of generating one from resource and operation
     */
    String permissionName() default "";
    
    /**
     * Whether this operation requires approval
     * @return true if approval is required, false otherwise
     */
    boolean requiresApproval() default false;
}
