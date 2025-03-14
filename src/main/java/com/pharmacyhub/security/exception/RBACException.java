package com.pharmacyhub.security.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception class for RBAC-related errors
 */
@Getter
public class RBACException extends RuntimeException {
    private final HttpStatus status;

    public RBACException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public RBACException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public RBACException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Create an exception for entity not found
     */
    public static RBACException entityNotFound(String entity) {
        return new RBACException(entity + " not found", HttpStatus.NOT_FOUND);
    }

    /**
     * Create an exception for duplicate entity
     */
    public static RBACException duplicateEntity(String message) {
        return new RBACException(message, HttpStatus.CONFLICT);
    }

    /**
     * Create an exception for invalid operation
     */
    public static RBACException invalidOperation(String message) {
        return new RBACException(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Create an exception for invalid input
     */
    public static RBACException invalidInput(String message) {
        return new RBACException(message, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Create an exception for invalid data
     */
    public static RBACException invalidData(String message) {
        return new RBACException("Invalid data: " + message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Create an exception for entity that already exists
     */
    public static RBACException alreadyExists(String message) {
        return new RBACException("Entity already exists: " + message, HttpStatus.CONFLICT);
    }

    /**
     * Create an exception for invalid role hierarchy
     */
    public static RBACException invalidRoleHierarchy() {
        return new RBACException("Invalid role hierarchy: Circular dependency detected", HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Get the error code for this exception
     */
    public String getErrorCode() {
        if (this.status == HttpStatus.NOT_FOUND) {
            return "RBAC_404";
        } else if (this.status == HttpStatus.CONFLICT) {
            return "RBAC_409";
        } else if (this.status == HttpStatus.BAD_REQUEST) {
            return "RBAC_400";
        } else if (this.status == HttpStatus.FORBIDDEN) {
            return "RBAC_403";
        } else {
            return "RBAC_500";
        }
    }
}
