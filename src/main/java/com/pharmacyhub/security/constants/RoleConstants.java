package com.pharmacyhub.security.constants;

/**
 * Constants for role names
 * Used to ensure consistent naming between frontend and backend
 */
public final class RoleConstants {
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ADMIN = "ADMIN";
    public static final String MANAGER = "MANAGER";
    public static final String PHARMACY_MANAGER = "PHARMACY_MANAGER";
    public static final String USER = "USER";
    public static final String PHARMACIST = "PHARMACIST";
    public static final String PROPRIETOR = "PROPRIETOR";
    public static final String SALESMAN = "SALESMAN";
    public static final String INSTRUCTOR = "INSTRUCTOR";
    
    private RoleConstants() {
        // Private constructor to prevent instantiation
    }
}