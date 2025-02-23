package com.pharmacyhub.security.exception;

import lombok.Getter;

@Getter
public class RBACException extends RuntimeException {
    private final String errorCode;

    public RBACException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public static RBACException permissionDenied() {
        return new RBACException("Permission denied", "RBAC_001");
    }

    public static RBACException invalidRoleHierarchy() {
        return new RBACException("Invalid role hierarchy detected", "RBAC_002");
    }

    public static RBACException entityNotFound(String entity) {
        return new RBACException(entity + " not found", "RBAC_003");
    }

    public static RBACException invalidOperation(String message) {
        return new RBACException(message, "RBAC_004");
    }
}