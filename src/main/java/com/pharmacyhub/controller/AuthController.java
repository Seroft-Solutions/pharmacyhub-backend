package com.pharmacyhub.controller;

import com.pharmacyhub.controller.base.BaseController;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.dto.request.LoginRequestDTO;
import com.pharmacyhub.dto.request.PasswordResetCompleteDTO;
import com.pharmacyhub.dto.request.PasswordResetRequestDTO;
import com.pharmacyhub.dto.request.SocialLoginRequestDTO;
import com.pharmacyhub.dto.request.UserCreateRequestDTO;
import com.pharmacyhub.dto.request.VerificationResendRequestDTO;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.response.ApiError;
import com.pharmacyhub.dto.response.AuthResponseDTO;
import com.pharmacyhub.dto.response.TokensDTO;
import com.pharmacyhub.dto.response.UserResponseDTO;
import com.pharmacyhub.security.infrastructure.exception.UnverifiedAccountException;
import com.pharmacyhub.dto.session.LoginValidationRequestDTO;
import com.pharmacyhub.dto.session.LoginValidationResultDTO;
import com.pharmacyhub.dto.session.LoginValidationResultDTO.LoginStatus;
import com.pharmacyhub.entity.session.LoginSession;
import com.pharmacyhub.service.session.SessionValidationService;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.service.AuthenticationService;
import com.pharmacyhub.service.EmailService;
import com.pharmacyhub.service.TokenService;
import com.pharmacyhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.pharmacyhub.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API endpoints for authentication and user management")
public class AuthController extends BaseController {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserService userService;
    @Autowired
    private SessionValidationService sessionValidationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private AuthService authService;

    @Value("${pharmacyhub.security.jwt.token-validity-in-seconds:18000}")
    private long tokenValidityInSeconds;
    
    @Value("${pharmacyhub.frontend.url}")
    private String frontendUrl;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signup")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<String>> signup(
            @Valid @RequestBody UserCreateRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            // Convert request to entity
            UserDTO userDTO = mapToEntity(request, UserDTO.class);

            // Get device information for verification email
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            // Pass additional device info for verification email
            userDTO.setIpAddress(ipAddress);
            userDTO.setUserAgent(userAgent);

            // Save user and send verification email
            PHUserDTO createdUser = userService.saveUserAndSendVerification(userDTO);

            if (createdUser != null) {
                return successResponse("User registered successfully. Please check your email for verification instructions.");
            }

            return errorResponse(HttpStatus.CONFLICT, "User with this email already exists");
        } catch (Exception e) {
            logger.error("Error during user registration", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed. Please try again later.");
        }
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify user email with token")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        try {
            // Validate token and verify user
            boolean isVerified = userService.verifyUser(token);

            if (isVerified) {
                ApiResponse<String> response = ApiResponse.<String>builder()
                        .status(HttpStatus.FOUND.value())
                        .data("Email verification successful")
                        .build();

                // Redirect to verification success page
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, frontendUrl + "/verification-successful")
                        .body(response);
            } else {
                ApiResponse<String> response = ApiResponse.<String>builder()
                        .status(HttpStatus.FOUND.value())
                        .error(new ApiError(HttpStatus.BAD_REQUEST.value(), "Email verification failed. Invalid or expired token."))
                        .build();

                // Redirect to verification failed page
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, frontendUrl + "/verification-failed")
                        .body(response);
            }
        } catch (Exception e) {
            logger.error("Error during email verification", e);
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .status(HttpStatus.FOUND.value())
                    .error(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Email verification failed due to server error."))
                    .build();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, frontendUrl + "/verification-failed")
                    .body(response);
        }
    }

    @GetMapping("/test")
    @Operation(summary = "Test endpoint to get all users")
    public ResponseEntity<ApiResponse<List<User>>> test() {
        List<User> users = userService.findAll();
        return successResponse(users);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification link")
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @Valid @RequestBody VerificationResendRequestDTO request,
            HttpServletRequest httpRequest) {

        // For security reasons, always return success even if user doesn't exist
        try {
            // Get device information
            String ipAddress = request.getIpAddress();
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = getClientIpAddress(httpRequest);
            }

            String userAgent = request.getUserAgent();
            if (userAgent == null || userAgent.isBlank()) {
                userAgent = httpRequest.getHeader("User-Agent");
            }
            
            // Use the auth service
            authService.resendVerificationEmail(request.getEmailAddress(), ipAddress, userAgent);
            
            return successResponse("If an account exists with that email, a verification link has been sent.");
        } catch (Exception e) {
            logger.error("Failed to send verification email", e);
            // Still return success for security (don't confirm if email exists)
            return successResponse("If an account exists with that email, a verification link has been sent.");
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody PasswordResetRequestDTO request,
            HttpServletRequest httpRequest) {
        // Get device information
        String ipAddress = request.getIpAddress();
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = getClientIpAddress(httpRequest);
        }

        String userAgent = request.getUserAgent();
        if (userAgent == null || userAgent.isBlank()) {
            userAgent = httpRequest.getHeader("User-Agent");
        }
        
        // Use synchronous method to avoid duplicate emails
        try {
            authService.requestPasswordReset(request.getEmailAddress(), ipAddress, userAgent);
        } catch (Exception e) {
            // Log the error but still return success for security
            logger.error("Error processing password reset", e);
        }
        
        // For security reasons, always return success immediately
        return successResponse("If an account exists with that email, a password reset link will be sent.");
    }

    @RequestMapping(value = "/password/validate-token/{token}", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Validate a password reset token")
    public ResponseEntity<ApiResponse<Boolean>> validatePasswordResetToken(@PathVariable String token) {
        try {
            // Use the token service to validate the token
            Long userId = tokenService.validateToken(token, "reset-password");
            
            // Return true if token is valid, false otherwise
            return successResponse(userId != null);
        } catch (Exception e) {
            logger.error("Failed to validate password reset token", e);
            return errorResponse(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody PasswordResetCompleteDTO request) {
        try {
            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return errorResponse(HttpStatus.BAD_REQUEST, "Passwords do not match.");
            }
            
            // Use the auth service
            boolean success = authService.completePasswordReset(
                request.getToken(), 
                request.getNewPassword(), 
                request.getConfirmPassword()
            );
            
            if (success) {
                return successResponse("Password has been reset successfully. Please log in with your new password.");
            } else {
                return errorResponse(HttpStatus.BAD_REQUEST, "Failed to reset password. Please try again.");
            }
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to reset password", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reset password. Please try again later.");
        }
    }

    @PostMapping("/social/callback")
    @Operation(summary = "Process social login callback")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> socialLoginCallback(
            @Valid @RequestBody SocialLoginRequestDTO request,
            HttpServletRequest httpRequest) {

        try {
            // Use the auth service to handle social login
            AuthResponseDTO response = authService.socialLogin(request, httpRequest);
            return successResponse(response);
        } catch (Exception e) {
            logger.error("Social login error", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process social login: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and generate JWT token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            // Use the auth service to handle login
            AuthResponseDTO response = authService.login(request, httpRequest);
            logger.info("Login processed successfully");
            return successResponse(response);
        } catch (UnverifiedAccountException ex) {
            logger.warn("Login attempt to unverified account: {}", request.getEmailAddress());
            return errorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            logger.error("Login failed for user: {}", request.getEmailAddress(), ex);
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate current session")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String sessionId) {
        try {
            // Use the auth service
            boolean success = authService.logout(authHeader, sessionId);
            
            if (success) {
                return successResponse("Logged out successfully");
            } else {
                return errorResponse(HttpStatus.BAD_REQUEST, "Failed to logout. Please try again.");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid session UUID: {}", sessionId, e);
            return errorResponse(HttpStatus.BAD_REQUEST, "Invalid session ID format");
        } catch (Exception e) {
            logger.error("Error during logout", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing logout");
        }
    }

    @PostMapping("/force-logout")
    @Operation(summary = "Force logout from other devices, keeping current session active")
    public ResponseEntity<ApiResponse<String>> forceLogout(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String currentSessionId) {
        try {
            // Use the auth service
            int invalidatedCount = authService.forceLogout(authHeader, currentSessionId);

            if (invalidatedCount > 0) {
                return successResponse(String.format("Successfully logged out from %d other device(s)", invalidatedCount));
            } else {
                return successResponse("No other active sessions found");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid session UUID: {}", currentSessionId, e);
            return errorResponse(HttpStatus.BAD_REQUEST, "Invalid session ID format");
        } catch (Exception e) {
            logger.error("Error during force logout", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing force logout request");
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // For multiple IP addresses, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    /**
     * Build a JSON string from the device-specific fields
     */
    /**
     * Convert User entity to UserDTO
     *
     * @param user The User entity to convert
     * @return The converted UserDTO
     */
    private UserDTO convertToUserDTO(User user) {
        if (user == null) return null;
        
        return UserDTO.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .emailAddress(user.getEmailAddress())
            .contactNumber(user.getContactNumber())
            .userType(user.getUserType())
            .registered(user.isRegistered())
            .openToConnect(user.isOpenToConnect())
            // Add any other fields that need to be mapped
            .build();
    }
    
    private String buildMetadataJson(LoginRequestDTO request) {
        // Using a more direct approach with StringBuilder
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Add screen dimensions if available
        if (request.getScreenWidth() != null && request.getScreenHeight() != null) {
            json.append("\"screen\":{");
            json.append("\"width\":\"" + request.getScreenWidth() + "\",");
            json.append("\"height\":\"" + request.getScreenHeight() + "\"");
            json.append("},");
        }

        // Add color depth if available
        if (request.getColorDepth() != null) {
            json.append("\"colorDepth\":\"" + request.getColorDepth() + "\",");
        }

        // Add timezone if available
        if (request.getTimezone() != null) {
            json.append("\"timezone\":\"" + request.getTimezone() + "\",");
        }

        // Add platform and language
        if (request.getPlatform() != null) {
            json.append("\"platform\":\"" + request.getPlatform() + "\",");
        }

        if (request.getLanguage() != null) {
            json.append("\"language\":\"" + request.getLanguage() + "\",");
        }

        // Add timestamp
        json.append("\"timestamp\":" + System.currentTimeMillis());

        json.append("}");
        return json.toString();
    }
    
    /**
     * Build a JSON string from the social login request
     */
    private String buildMetadataJson(SocialLoginRequestDTO request) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Add screen dimensions if available
        if (request.getScreenWidth() != null && request.getScreenHeight() != null) {
            json.append("\"screen\":{\"width\":\"" + request.getScreenWidth() + "\",\"height\":\"" + request.getScreenHeight() + "\"}");
            json.append(",");
        }
        
        // Add color depth if available
        if (request.getColorDepth() != null) {
            json.append("\"colorDepth\":\"" + request.getColorDepth() + "\"");
            json.append(",");
        }
        
        // Add timezone if available
        if (request.getTimezone() != null) {
            json.append("\"timezone\":\"" + request.getTimezone() + "\"");
            json.append(",");
        }
        
        // Add platform and language
        if (request.getPlatform() != null) {
            json.append("\"platform\":\"" + request.getPlatform() + "\"");
            json.append(",");
        }
        
        if (request.getLanguage() != null) {
            json.append("\"language\":\"" + request.getLanguage() + "\"");
            json.append(",");
        }
        
        // Add source flag for social login
        json.append("\"source\":\"social\"");
        json.append(",");
        
        // Add timestamp
        json.append("\"timestamp\":" + System.currentTimeMillis());
        
        json.append("}");
        return json.toString();
    }
}
