package com.pharmacyhub.controller;

import com.pharmacyhub.controller.base.BaseController;
import com.pharmacyhub.dto.request.SocialLoginRequestDTO;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.response.AuthResponseDTO;
import com.pharmacyhub.dto.response.TokensDTO;
import com.pharmacyhub.dto.response.UserResponseDTO;
import com.pharmacyhub.dto.session.LoginValidationRequestDTO;
import com.pharmacyhub.dto.session.LoginValidationResultDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.service.AuthenticationService;
import com.pharmacyhub.service.session.SessionValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/social-auth")
@Tag(name = "Social Authentication", description = "API endpoints for social authentication")
public class SocialAuthController extends BaseController {

    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private SessionValidationService sessionValidationService;
    
    @Value("${pharmacyhub.security.jwt.token-validity-in-seconds:18000}")
    private long tokenValidityInSeconds;
    
    private static final Logger logger = LoggerFactory.getLogger(SocialAuthController.class);

    @PostMapping("/google/callback")
    @Operation(summary = "Process Google login callback")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> handleGoogleCallback(
            @Valid @RequestBody SocialLoginRequestDTO request,
            HttpServletRequest httpRequest) {
        
        try {
            logger.info("Received Google login callback with code: {}, callback URL: {}", 
                request.getCode() != null ? "[PRESENT]" : "[MISSING]",
                request.getCallbackUrl() != null ? request.getCallbackUrl() : "[NOT PROVIDED]");
                
            // If callbackUrl is not provided, use the default redirect URI from google credentials
            String callbackUrl = request.getCallbackUrl();
            if (callbackUrl == null || callbackUrl.isEmpty()) {
                callbackUrl = "http://localhost:3000/auth/callback";
                logger.info("No callback URL provided, using default: {}", callbackUrl);
            }
                
            // Get device information
            String ipAddress = request.getIpAddress();
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = getClientIpAddress(httpRequest);
            }
            
            String userAgent = request.getUserAgent();
            if (userAgent == null || userAgent.isBlank()) {
                userAgent = httpRequest.getHeader("User-Agent");
            }
            
            // Process the Google login
            User authenticatedUser = authenticationService.processSocialLogin(
                request.getCode(), 
                callbackUrl
            );
            
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

            // Ensure the user has at least one role - add USER role if none exists
            if (userRoles.isEmpty()) {
                logger.warn("User {} has no roles, adding default USER role", authenticatedUser.getEmailAddress());
                userRoleService.assignRoleToUser(authenticatedUser.getId(), "USER");
                // Update role names for response
                roleNames.add("USER");
                userResponse.setRoles(roleNames);
            }

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

            logger.info("Google login successful for user: {}", authenticatedUser.getEmailAddress());
            return successResponse(response);
                
        } catch (Exception e) {
            logger.error("Google login error", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process Google login: " + e.getMessage());
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
        json.append("\"source\":\"google\"");
        json.append(",");
        
        // Add timestamp
        json.append("\"timestamp\":" + System.currentTimeMillis());
        
        json.append("}");
        return json.toString();
    }
}
