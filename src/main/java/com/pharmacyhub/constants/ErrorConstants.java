package com.pharmacyhub.constants;

/**
 * Constants for error messages and codes used across the application
 */
public final class ErrorConstants {
    
    private ErrorConstants() {
        // Private constructor to prevent instantiation
    }
    
    // HTTP Status Codes
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_CONFLICT = 409;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;
    
    // Error Messages
    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later.";
    public static final String VALIDATION_ERROR = "Validation failed. Please check your input.";
    public static final String RESOURCE_NOT_FOUND = "The requested resource was not found.";
    public static final String ACCESS_DENIED = "You don't have permission to access this resource.";
    public static final String AUTHENTICATION_FAILED = "Authentication failed. Please check your credentials.";
    public static final String ACCOUNT_DISABLED = "Your account is disabled. Please contact support.";
    public static final String ACCOUNT_LOCKED = "Your account is locked. Please contact support.";
    public static final String ACCOUNT_EXPIRED = "Your account has expired. Please contact support.";
    public static final String INVALID_TOKEN = "Invalid or expired token.";
    public static final String METHOD_NOT_ALLOWED = "The requested method is not allowed.";
    public static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported media type.";
    public static final String DUPLICATE_RESOURCE = "Resource already exists.";
    
    // Error Codes
    public static final String CODE_INTERNAL_ERROR = "ERR_INTERNAL";
    public static final String CODE_VALIDATION = "ERR_VALIDATION";
    public static final String CODE_NOT_FOUND = "ERR_NOT_FOUND";
    public static final String CODE_ACCESS_DENIED = "ERR_ACCESS_DENIED";
    public static final String CODE_AUTHENTICATION = "ERR_AUTHENTICATION";
    public static final String CODE_ACCOUNT_DISABLED = "ERR_ACCOUNT_DISABLED";
    public static final String CODE_ACCOUNT_LOCKED = "ERR_ACCOUNT_LOCKED";
    public static final String CODE_ACCOUNT_EXPIRED = "ERR_ACCOUNT_EXPIRED";
    public static final String CODE_INVALID_TOKEN = "ERR_INVALID_TOKEN";
    public static final String CODE_METHOD_NOT_ALLOWED = "ERR_METHOD_NOT_ALLOWED";
    public static final String CODE_UNSUPPORTED_MEDIA = "ERR_UNSUPPORTED_MEDIA";
    public static final String CODE_DUPLICATE = "ERR_DUPLICATE_RESOURCE";
}
