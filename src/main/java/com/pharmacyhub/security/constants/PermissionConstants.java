package com.pharmacyhub.security.constants;

/**
 * Constants for permission names
 * Used to ensure consistent naming between frontend and backend
 */
public final class PermissionConstants {
    // User Management
    public static final String MANAGE_USERS = "manage:users";
    public static final String VIEW_USERS = "view:users";
    
    // Pharmacy Management
    public static final String CREATE_PHARMACY = "create:pharmacy";
    public static final String EDIT_PHARMACY = "edit:pharmacy";
    public static final String DELETE_PHARMACY = "delete:pharmacy";
    public static final String VIEW_PHARMACY = "view:pharmacy";
    
    // Pharmacist Management
    public static final String CREATE_PHARMACIST = "create:pharmacist";
    public static final String UPDATE_PHARMACIST = "update:pharmacist";
    public static final String VIEW_PHARMACIST = "view:pharmacist";
    public static final String VIEW_ALL_PHARMACISTS = "view:all:pharmacists";
    public static final String DELETE_PHARMACIST = "delete:pharmacist";
    
    // Connection Management
    public static final String MANAGE_CONNECTIONS = "manage:connections";
    public static final String VIEW_CONNECTIONS = "view:connections";
    public static final String VIEW_ALL_CONNECTIONS = "view:all:connections";
    public static final String APPROVE_CONNECTIONS = "approve:connections";
    public static final String REJECT_CONNECTIONS = "reject:connections";
    
    // Role Management
    public static final String MANAGE_ROLES = "manage:roles";
    
    // Permission Management
    public static final String MANAGE_PERMISSIONS = "manage:permissions";
    
    // Group Management
    public static final String MANAGE_GROUPS = "manage:groups";
    
    // Exam Management
    public static final String MANAGE_EXAMS = "manage:exams";
    public static final String TAKE_EXAMS = "take:exams";
    public static final String GRADE_EXAMS = "grade:exams";
    
    // System Management
    public static final String MANAGE_SYSTEM = "manage:system";
    public static final String MANAGE_SYSTEM_SETTINGS = "manage:system:settings";
    
    // Audit Management
    public static final String VIEW_AUDIT_LOGS = "view:audit:logs";
    public static final String VIEW_OWN_AUDIT_LOGS = "view:own:audit:logs";
    public static final String EXPORT_AUDIT_LOGS = "export:audit:logs";
    
    // Order Management
    public static final String APPROVE_ORDERS = "approve:orders";
    public static final String PLACE_ORDERS = "place:orders";
    
    // Inventory Management
    public static final String MANAGE_INVENTORY = "manage:inventory";
    public static final String VIEW_PRODUCTS = "view:products";
    
    // Reports
    public static final String VIEW_REPORTS = "view:reports";
    
    // Status Updates
    public static final String UPDATE_STATUS = "update:status";
    
    private PermissionConstants() {
        // Private constructor to prevent instantiation
    }
}