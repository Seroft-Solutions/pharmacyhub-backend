package com.pharmacyhub.exception;

import org.springframework.http.HttpStatus;

/**
 * Enumeration of standardized exception constants used across the application.
 * Each constant includes:
 * - Error code
 * - HTTP status
 * - Default message
 * - Default resolution
 */
public enum ExceptionConstants {
    
    // Authentication related exceptions
    AUTHENTICATION_FAILED("ERR_AUTHENTICATION", HttpStatus.UNAUTHORIZED, 
            "Authentication failed. Please check your credentials.",
            "Please ensure you entered the correct username and password. If you forgot your password, use the 'Forgot Password' option."),
    
    INVALID_TOKEN("ERR_INVALID_TOKEN", HttpStatus.UNAUTHORIZED, 
            "Invalid or expired token.",
            "Your session may have expired. Please log in again."),
    
    ACCOUNT_DISABLED("ERR_ACCOUNT_DISABLED", HttpStatus.UNAUTHORIZED, 
            "Your account is disabled.",
            "Please contact support for assistance with reactivating your account."),
    
    ACCOUNT_LOCKED("ERR_ACCOUNT_LOCKED", HttpStatus.UNAUTHORIZED, 
            "Your account is locked.",
            "This typically happens after multiple failed login attempts. Please wait 30 minutes or contact support."),
    
    ACCOUNT_EXPIRED("ERR_ACCOUNT_EXPIRED", HttpStatus.UNAUTHORIZED, 
            "Your account has expired.",
            "Please contact support to renew your subscription or account."),
    
    // Authorization related exceptions
    ACCESS_DENIED("ERR_ACCESS_DENIED", HttpStatus.FORBIDDEN, 
            "You don't have permission to access this resource.",
            "Please ensure you have the necessary permissions or contact your administrator."),
    
    // Resource related exceptions
    RESOURCE_NOT_FOUND("ERR_NOT_FOUND", HttpStatus.NOT_FOUND, 
            "The requested resource was not found.",
            "Please check the resource identifier and try again. If the issue persists, the resource may have been deleted."),
    
    DUPLICATE_RESOURCE("ERR_DUPLICATE_RESOURCE", HttpStatus.CONFLICT, 
            "Resource already exists.",
            "A resource with the same identifier already exists. Please use a different identifier or update the existing resource."),
    
    // Validation related exceptions
    VALIDATION_ERROR("ERR_VALIDATION", HttpStatus.BAD_REQUEST, 
            "Validation failed. Please check your input.",
            "Please review the provided details and correct any invalid fields."),
    
    // Session related exceptions
    SESSION_CONFLICT("ERR_SESSION_CONFLICT", HttpStatus.CONFLICT, 
            "A conflicting session was detected.",
            "You appear to be logged in from another device or browser. Please log out from other sessions or continue on this device."),
    
    SESSION_EXPIRED("ERR_SESSION_EXPIRED", HttpStatus.UNAUTHORIZED, 
            "Your session has expired.",
            "For security reasons, sessions expire after a period of inactivity. Please log in again."),
    
    SESSION_NOT_FOUND("ERR_SESSION_NOT_FOUND", HttpStatus.NOT_FOUND, 
            "Session not found.",
            "Your session could not be found. Please log in again."),
    
    SESSION_VALIDATION_ERROR("ERR_SESSION_VALIDATION", HttpStatus.BAD_REQUEST, 
            "Session validation failed.",
            "There was an issue with your session. Please log in again."),
    
    // HTTP related exceptions
    METHOD_NOT_ALLOWED("ERR_METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, 
            "The requested method is not allowed.",
            "Please check the API documentation for the correct HTTP method to use for this endpoint."),
    
    UNSUPPORTED_MEDIA("ERR_UNSUPPORTED_MEDIA", HttpStatus.UNSUPPORTED_MEDIA_TYPE, 
            "Unsupported media type.",
            "The API does not support the provided content type. Please check the API documentation for supported content types."),
    
    // Server related exceptions
    INTERNAL_ERROR("ERR_INTERNAL", HttpStatus.INTERNAL_SERVER_ERROR, 
            "An unexpected error occurred. Please try again later.",
            "This is a server-side issue. Please try again later or contact support if the issue persists.");
    
    private final String code;
    private final HttpStatus status;
    private final String message;
    private final String resolution;
    
    ExceptionConstants(String code, HttpStatus status, String message, String resolution) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.resolution = resolution;
    }
    
    public String getCode() {
        return code;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getResolution() {
        return resolution;
    }
}
