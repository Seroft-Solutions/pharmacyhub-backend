package com.pharmacyhub.service;

import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.dto.request.LoginRequestDTO;
import com.pharmacyhub.dto.request.SocialLoginRequestDTO;
import com.pharmacyhub.dto.response.AuthResponseDTO;
import com.pharmacyhub.dto.response.TokensDTO;
import com.pharmacyhub.dto.response.UserResponseDTO;
import com.pharmacyhub.dto.session.LoginValidationRequestDTO;
import com.pharmacyhub.dto.session.LoginValidationResultDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.exception.UnverifiedAccountException;
import com.pharmacyhub.security.service.AuthenticationService;
import com.pharmacyhub.service.session.SessionValidationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for handling authentication-related operations
 */
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

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

    @Value("${pharmacyhub.security.jwt.token-validity-in-seconds:18000}")
    private long tokenValidityInSeconds;
    
    @Value("${pharmacyhub.frontend.url}")
    private String frontendUrl;
    
    /**
     * Process user login
     * 
     * @param request The login request
     * @param httpRequest The HTTP request for extracting client IP
     * @return Login response with user details and tokens
     * @throws UnverifiedAccountException if the account is not verified
     * @throws Exception if login fails due to invalid credentials
     */
    public AuthResponseDTO login(LoginRequestDTO request, HttpServletRequest httpRequest) throws Exception {
        // Authenticate the user
        User authenticatedUser = authenticationService.authenticateUser(request.getEmailAddress(), request.getPassword());
        
        // Generate JWT token
        String token = authenticationService.generateToken(authenticatedUser);
        
        // Get user roles
        Set<Role> userRoles = authenticatedUser.getRoles();
        List<String> roleNames = userRoles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        
        // Get user permissions
        Set<String> permissionNames = new HashSet<>();
        for (Role userRole : userRoles) {
            if (userRole.getPermissions() != null) {
                userRole.getPermissions().stream()
                        .map(Permission::getName)
                        .forEach(permissionNames::add);
            }
        }
        
        // Create user response DTO
        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(authenticatedUser.getId().toString())
                .email(authenticatedUser.getEmailAddress())
                .firstName(authenticatedUser.getFirstName())
                .lastName(authenticatedUser.getLastName())
                .active(authenticatedUser.isEnabled())
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .roles(roleNames)
                .build();
        
        // Create tokens DTO
        TokensDTO tokens = TokensDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenValidityInSeconds)
                .build();
        
        // Create response DTO
        AuthResponseDTO response = AuthResponseDTO.builder()
                .user(userResponse)
                .tokens(tokens)
                .build();
        
        // Validate session if device information is provided
        if (request.getDeviceId() != null) {
            // Get client IP from request
            String ipAddress = getClientIpAddress(httpRequest);
            
            // Validate login session
            LoginValidationRequestDTO validationRequest = LoginValidationRequestDTO.builder()
                .userId(authenticatedUser.getId())
                .deviceId(request.getDeviceId())
                .ipAddress(ipAddress)
                .userAgent(request.getUserAgent())
                .platform(request.getPlatform())
                .language(request.getLanguage())
                .metadata(buildMetadataJson(request))
                .build();
            
            LoginValidationResultDTO validationResult = sessionValidationService.validateLogin(validationRequest);
            
            // Add session ID to response if available
            if (validationResult.getSessionId() != null) {
                response = AuthResponseDTO.builder()
                    .user(userResponse)
                    .tokens(tokens)
                    .sessionId(validationResult.getSessionId())
                    .validationStatus(validationResult.getStatus().toString())
                    .build();
            }
        }
        
        logger.info("Login successful for user: {}", authenticatedUser.getUsername());
        return response;
    }
    
    /**
     * Process social login
     * 
     * @param request The social login request
     * @param httpRequest The HTTP request for extracting client IP
     * @return Login response with user details and tokens
     * @throws Exception if social login processing fails
     */
    public AuthResponseDTO socialLogin(SocialLoginRequestDTO request, HttpServletRequest httpRequest) throws Exception {
        logger.info("Received social login callback with code: {}",
            request.getCode() != null ? "[PRESENT]" : "[MISSING]");
        
        // Get device information
        String ipAddress = request.getIpAddress();
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = getClientIpAddress(httpRequest);
        }
        
        String userAgent = request.getUserAgent();
        if (userAgent == null || userAgent.isBlank()) {
            userAgent = httpRequest.getHeader("User-Agent");
        }
        
        // Process the social login
        User authenticatedUser = authenticationService.processSocialLogin(
            request.getCode(),
            request.getCallbackUrl()
        );
        
        // If this is a new user, they should be marked as verified automatically
        if (!authenticatedUser.isVerified()) {
            authenticatedUser.setVerified(true);
            userService.saveUser(authenticatedUser);
        }
        
        // Generate JWT token
        String token = authenticationService.generateToken(authenticatedUser);
        
        // Get user roles
        Set<Role> userRoles = authenticatedUser.getRoles();
        List<String> roleNames = userRoles.stream()
            .map(Role::getName)
            .collect(Collectors.toList());
        
        // Get user permissions
        Set<String> permissionNames = new HashSet<>();
        for (Role userRole : userRoles) {
            if (userRole.getPermissions() != null) {
                userRole.getPermissions().stream()
                        .map(Permission::getName)
                        .forEach(permissionNames::add);
            }
        }
        
        // Create user response DTO
        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(authenticatedUser.getId().toString())
                .email(authenticatedUser.getEmailAddress())
                .firstName(authenticatedUser.getFirstName())
                .lastName(authenticatedUser.getLastName())
                .active(authenticatedUser.isEnabled())
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .roles(roleNames)
                .build();
        
        // Create tokens DTO
        TokensDTO tokens = TokensDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenValidityInSeconds)
                .build();
        
        // Create response DTO
        AuthResponseDTO response = AuthResponseDTO.builder()
                .user(userResponse)
                .tokens(tokens)
                .build();
        
        // Validate session if device information is provided
        if (request.getDeviceId() != null) {
            // Create validation request
            LoginValidationRequestDTO validationRequest = LoginValidationRequestDTO.builder()
                .userId(authenticatedUser.getId())
                .deviceId(request.getDeviceId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .platform(request.getPlatform())
                .language(request.getLanguage())
                .metadata(buildMetadataJson(request))
                .build();
            
            LoginValidationResultDTO validationResult = sessionValidationService.validateLogin(validationRequest);
            
            // Add session ID to response if available
            if (validationResult.getSessionId() != null) {
                response = AuthResponseDTO.builder()
                    .user(userResponse)
                    .tokens(tokens)
                    .sessionId(validationResult.getSessionId())
                    .validationStatus(validationResult.getStatus().toString())
                    .build();
            }
        }
        
        logger.info("Social login successful for user: {}", authenticatedUser.getEmailAddress());
        return response;
    }
    
    /**
     * Process user logout
     * 
     * @param authHeader The Authorization header containing JWT token
     * @param sessionId Optional session ID to invalidate specific session
     * @return True if logout was successful
     * @throws Exception if logout processing fails
     */
    public boolean logout(String authHeader, String sessionId) throws Exception {
        // Extract user ID from JWT token
        String token = authHeader.replace("Bearer ", "");
        Long userId = authenticationService.getUserIdFromToken(token);
        
        if (userId == null) {
            throw new Exception("Invalid token");
        }
        
        if (sessionId != null && !sessionId.isEmpty()) {
            // Invalidate specific session if session ID is provided
            try {
                UUID sessionUUID = UUID.fromString(sessionId);
                boolean invalidated = sessionValidationService.invalidateSession(sessionUUID);
                if (!invalidated) {
                    logger.warn("Session not found or already inactive: {}", sessionId);
                }
                return invalidated;
            } catch (IllegalArgumentException e) {
                logger.error("Invalid session UUID: {}", sessionId, e);
                throw new IllegalArgumentException("Invalid session ID format");
            }
        } else {
            // Default behavior: Invalidate all sessions for the user
            sessionValidationService.invalidateAllSessions(userId);
            return true;
        }
    }
    
    /**
     * Process force logout from other devices
     * 
     * @param authHeader The Authorization header containing JWT token
     * @param currentSessionId Current session ID to keep active
     * @return Number of invalidated sessions
     * @throws Exception if force logout processing fails
     */
    public int forceLogout(String authHeader, String currentSessionId) throws Exception {
        // Extract user ID from JWT token
        String token = authHeader.replace("Bearer ", "");
        Long userId = authenticationService.getUserIdFromToken(token);
        
        if (userId == null) {
            throw new Exception("Invalid token");
        }
        
        // Validate session ID format
        UUID sessionUUID;
        try {
            sessionUUID = UUID.fromString(currentSessionId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid session UUID: {}", currentSessionId, e);
            throw new IllegalArgumentException("Invalid session ID format");
        }
        
        // Invalidate all sessions except the current one
        return sessionValidationService.invalidateOtherSessions(userId, sessionUUID);
    }
    
    /**
     * Process email verification
     * 
     * @param token The verification token
     * @return True if verification was successful
     * @throws Exception if verification processing fails
     */
    public boolean verifyEmail(String token) throws Exception {
        return userService.verifyUser(token);
    }
    
    /**
     * Resend verification email
     * 
     * @param email The email address
     * @param ipAddress The IP address
     * @param userAgent The user agent
     * @return True if email was sent successfully
     * @throws Exception if email sending fails
     */
    public boolean resendVerificationEmail(String email, String ipAddress, String userAgent) throws Exception {
        // Find user
        User user = userService.findByEmail(email);
        if (user == null) {
            // Return true for security reasons (don't reveal if email exists)
            return true;
        }
        
        // If user is already verified, return true but don't send email
        if (user.isVerified()) {
            return true;
        }
        
        // Generate a verification token
        String token = tokenService.generateToken(user.getId(), "email-verification");
        
        // Send verification email with device tracking information
        emailService.sendVerificationEmail(user.getEmailAddress(), token, ipAddress, userAgent);
        
        return true;
    }
    
    /**
     * Process password reset request
     * 
     * @param email The email address
     * @param ipAddress The IP address
     * @param userAgent The user agent
     * @return True if password reset email was sent successfully
     * @throws Exception if email sending fails
     */
    /**
     * Asynchronously process password reset request
     * 
     * @param email The email address
     * @param ipAddress The IP address
     * @param userAgent The user agent
     * @return CompletableFuture with result of operation
     */
    @Async
    public CompletableFuture<Boolean> requestPasswordResetAsync(String email, String ipAddress, String userAgent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if user exists
                User user = userService.findByEmail(email);
                if (user == null) {
                    // Return true for security reasons (don't reveal if email exists)
                    return true;
                }
                
                // Generate a password reset token
                String token = tokenService.generateToken(user.getId(), "reset-password");
                
                // Send password reset email with device tracking information
                emailService.sendPasswordResetEmail(user.getEmailAddress(), token, ipAddress, userAgent);
                
                return true;
            } catch (Exception e) {
                logger.error("Failed to send password reset email", e);
                return false;
            }
        });
    }

    /**
     * Process password reset request - non-blocking version
     * 
     * @param email The email address
     * @param ipAddress The IP address
     * @param userAgent The user agent
     * @return True to indicate request was accepted
     */
    public boolean requestPasswordReset(String email, String ipAddress, String userAgent) {
        // Start the async process but don't wait for it to complete
        requestPasswordResetAsync(email, ipAddress, userAgent);
        
        // Return true immediately - the actual email sending will happen in background
        return true;
    }
    
    /**
     * Process password reset completion
     * 
     * @param token The reset token
     * @param newPassword The new password
     * @param confirmPassword The confirm password
     * @return True if password was reset successfully
     * @throws Exception if password reset fails
     */
    public boolean completePasswordReset(String token, String newPassword, String confirmPassword) throws Exception {
        // Add detailed logging
        logger.info("Processing password reset for token: {}", token);
        
        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            logger.warn("Password reset failed: passwords do not match");
            throw new IllegalArgumentException("Passwords do not match.");
        }
        
        // Validate token
        Long userId = tokenService.validateToken(token, "reset-password");
        if (userId == null) {
            logger.warn("Password reset failed: invalid or expired token: {}", token);
            throw new IllegalArgumentException("Invalid or expired token.");
        }
        
        // Find user
        User user = userService.findById(userId);
        if (user == null) {
            logger.warn("Password reset failed: user not found for ID: {}", userId);
            throw new IllegalArgumentException("User not found.");
        }
        
        logger.info("Resetting password for user: {}", user.getEmailAddress());
        
        // Update password
        user.setPassword(authenticationService.encodePassword(newPassword));
        userService.saveUser(user);
        
        // Invalidate token - do this AFTER successful password update
        tokenService.invalidateToken(token);
        
        // Invalidate all existing sessions for security
        sessionValidationService.invalidateAllSessions(userId);
        
        logger.info("Password reset successfully completed for user: {}", user.getEmailAddress());
        return true;
    }
    
    /**
     * Get client IP address from request
     */
    public String getClientIpAddress(HttpServletRequest request) {
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
