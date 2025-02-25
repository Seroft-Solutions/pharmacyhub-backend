package com.pharmacyhub.security.domain;

/**
 * Defines all resources that can be operated on in the system
 */
public enum ResourceType {
    // Users and authentication
    USER,
    
    // Roles and permissions
    ROLE,
    PERMISSION,
    GROUP,
    
    // Pharmacy staff
    PHARMACIST,
    PHARMACY_MANAGER,
    PROPRIETOR,
    SALESMAN,
    
    // Pharmacy operations
    PHARMACY,
    INVENTORY,
    MEDICINE,
    PRESCRIPTION,
    ORDER,
    SALES,
    
    // Business operations
    BUSINESS,
    REPORTS,
    ANALYTICS,
    
    // System
    CONNECTION,
    AUDIT_LOG,
    SYSTEM_SETTING,
    
    // Other
    NOTIFICATION,
    MESSAGE,
    DOCUMENT
}