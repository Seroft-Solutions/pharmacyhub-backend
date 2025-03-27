package com.pharmacyhub.constants;

/**
 * Constants for session-specific error messages and codes
 * Designed to align with frontend exception handling
 */
public final class SessionErrorConstants {
    
    private SessionErrorConstants() {
        // Private constructor to prevent instantiation
    }
    
    // Session Error Codes
    public static final String SESS_001 = "SESS_001"; // Multiple active sessions
    public static final String SESS_002 = "SESS_002"; // Suspicious location
    public static final String SESS_003 = "SESS_003"; // Session terminated
    public static final String SESS_004 = "SESS_004"; // New device
    public static final String SESS_005 = "SESS_005"; // OTP required
    public static final String SESS_006 = "SESS_006"; // Max devices reached
    public static final String SESS_007 = "SESS_007"; // Session expired
    
    // Session Error Messages
    public static final String MULTIPLE_ACTIVE_SESSIONS = "You are already logged in from another device.";
    public static final String SUSPICIOUS_LOCATION = "We detected a login attempt from an unusual location.";
    public static final String SESSION_TERMINATED = "Your session was terminated from another device.";
    public static final String NEW_DEVICE = "We detected a login attempt from a new device.";
    public static final String OTP_REQUIRED = "Additional verification is required for your security.";
    public static final String MAX_DEVICES_REACHED = "You have reached the maximum number of allowed devices.";
    public static final String SESSION_EXPIRED = "Your session has expired due to inactivity.";
    
    // Session Error Actions
    public static final String ACTION_LOGOUT_OTHERS = "Log out from the other device or click \"Log Out Other Devices\" to continue with this session.";
    public static final String ACTION_VERIFY_IDENTITY = "Verify your identity to continue or contact support if you didn't attempt to log in.";
    public static final String ACTION_LOGIN_AGAIN = "Please log in again to continue. If you didn't terminate your session, consider changing your password.";
    public static final String ACTION_VERIFY_NEW_DEVICE = "Verify your identity to continue using this new device.";
    public static final String ACTION_ENTER_OTP = "Please enter the verification code sent to your email or mobile device.";
    public static final String ACTION_REMOVE_DEVICE = "Please remove an existing device from your account settings before adding a new one.";
    public static final String ACTION_INACTIVE = "Please log in again to continue.";
    
    // Map LoginStatus to Error Codes
    public static String getErrorCode(String loginStatus) {
        switch (loginStatus) {
            case "TOO_MANY_DEVICES":
                return SESS_001;
            case "SUSPICIOUS_LOCATION":
                return SESS_002;
            case "NEW_DEVICE":
                return SESS_004;
            case "OTP_REQUIRED":
                return SESS_005;
            case "ACCOUNT_BLOCKED":
                return SESS_006;
            default:
                return null;
        }
    }
    
    // Map LoginStatus to Error Messages
    public static String getErrorMessage(String loginStatus) {
        switch (loginStatus) {
            case "TOO_MANY_DEVICES":
                return MULTIPLE_ACTIVE_SESSIONS;
            case "SUSPICIOUS_LOCATION":
                return SUSPICIOUS_LOCATION;
            case "NEW_DEVICE":
                return NEW_DEVICE;
            case "OTP_REQUIRED":
                return OTP_REQUIRED;
            case "ACCOUNT_BLOCKED":
                return MAX_DEVICES_REACHED;
            default:
                return null;
        }
    }
    
    // Map LoginStatus to Error Actions
    public static String getErrorAction(String loginStatus) {
        switch (loginStatus) {
            case "TOO_MANY_DEVICES":
                return ACTION_LOGOUT_OTHERS;
            case "SUSPICIOUS_LOCATION":
                return ACTION_VERIFY_IDENTITY;
            case "NEW_DEVICE":
                return ACTION_VERIFY_NEW_DEVICE;
            case "OTP_REQUIRED":
                return ACTION_ENTER_OTP;
            case "ACCOUNT_BLOCKED":
                return ACTION_REMOVE_DEVICE;
            default:
                return null;
        }
    }
}
