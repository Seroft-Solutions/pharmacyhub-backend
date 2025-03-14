package com.pharmacyhub.security.constants;

/**
 * Constants for authentication and user account-related permissions
 * These constants are shared between frontend and backend to ensure naming consistency
 */
public final class AuthPermissionConstants {
    /**
     * Authentication
     */
    public static final String LOGIN = "auth:login";
    public static final String LOGOUT = "auth:logout";
    public static final String REGISTER = "auth:register";
    
    /**
     * Account Management
     */
    public static final String MANAGE_ACCOUNT = "auth:manage-account";
    public static final String VERIFY_EMAIL = "auth:verify-email";
    public static final String RESET_PASSWORD = "auth:reset-password";
    
    /**
     * Profile
     */
    public static final String VIEW_PROFILE = "auth:view-profile";
    public static final String EDIT_PROFILE = "auth:edit-profile";
    
    /**
     * Sessions
     */
    public static final String MANAGE_SESSIONS = "auth:manage-sessions";
    public static final String VIEW_SESSIONS = "auth:view-sessions";
    
    /**
     * User Management (Admin)
     */
    public static final String MANAGE_USERS = "auth:manage-users";
    public static final String VIEW_USERS = "auth:view-users";
    public static final String EDIT_USERS = "auth:edit-users";
    public static final String DELETE_USERS = "auth:delete-users";
    public static final String IMPERSONATE_USER = "auth:impersonate-user";
    
    // Role-based permission groupings
    public static final String[] BASIC_USER_PERMISSIONS = {
        LOGIN, LOGOUT, VIEW_PROFILE, EDIT_PROFILE,
        MANAGE_ACCOUNT, VERIFY_EMAIL, RESET_PASSWORD,
        VIEW_SESSIONS
    };
    
    public static final String[] ADMIN_PERMISSIONS = {
        MANAGE_USERS, VIEW_USERS, EDIT_USERS, DELETE_USERS,
        MANAGE_SESSIONS, IMPERSONATE_USER
    };
    
    private AuthPermissionConstants() {
        // Private constructor to prevent instantiation
    }
}
