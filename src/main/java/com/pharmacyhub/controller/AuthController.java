package com.pharmacyhub.controller;

import com.pharmacyhub.controller.base.BaseController;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.dto.request.LoginRequestDTO;
import com.pharmacyhub.dto.request.UserCreateRequestDTO;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.response.ApiError;
import com.pharmacyhub.dto.response.AuthResponseDTO;
import com.pharmacyhub.dto.response.TokensDTO;
import com.pharmacyhub.dto.response.UserResponseDTO;
import com.pharmacyhub.dto.session.LoginValidationRequestDTO;
import com.pharmacyhub.dto.session.LoginValidationResultDTO;
import com.pharmacyhub.dto.session.LoginValidationResultDTO.LoginStatus;
import com.pharmacyhub.entity.session.LoginSession;
import com.pharmacyhub.service.session.SessionValidationService;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.service.AuthenticationService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API endpoints for authentication and user management")
public class AuthController extends BaseController
{
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserService userService;
    @Autowired
    private SessionValidationService sessionValidationService;
    
    @Value("${pharmacyhub.security.jwt.token-validity-in-seconds:18000}")
    private long tokenValidityInSeconds;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signup")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody UserCreateRequestDTO request) {
        // Convert request to entity
        UserDTO userDTO = mapToEntity(request, UserDTO.class);
        PHUserDTO createdUser = userService.saveUser(userDTO);

        if (createdUser != null) {
            return successResponse("User registered successfully. Please check your email for verification.");
        }
        
        return errorResponse(HttpStatus.CONFLICT, "User with this email already exists");
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify user email with token")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        boolean isVerified = userService.verifyUser(token);
        
        if (isVerified) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .status(HttpStatus.FOUND.value())
                    .data("Email verification successful")
                    .build();
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "https://pharmacyhub.pk/verification-successful")
                    .body(response);
        } else {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .status(HttpStatus.FOUND.value())
                    .error(new ApiError(HttpStatus.BAD_REQUEST.value(), "Email verification failed"))
                    .build();
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "https://pharmacyhub.pk/verification-failed")
                    .body(response);
        }
    }


    @GetMapping("/test")
    @Operation(summary = "Test endpoint to get all users")
    public ResponseEntity<ApiResponse<List<User>>> test() {
        List<User> users = userService.findAll();
        return successResponse(users);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and generate JWT token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest) {
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
                .expiresIn(tokenValidityInSeconds) // Use the configured token validity
                .build();

        // Create response DTO
        AuthResponseDTO response = AuthResponseDTO.builder()
                .user(userResponse)
                .tokens(tokens)
                .build();
        
        // Validate session if device information is provided
        if (request.getDeviceId() != null) {
            // Get client IP if not provided
            String ipAddress = request.getDeviceId();
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = getClientIpAddress(httpRequest);
            }
            
            // Validate login session
            LoginValidationRequestDTO validationRequest = LoginValidationRequestDTO.builder()
                .userId(authenticatedUser.getId())
                .deviceId(request.getDeviceId())
                .ipAddress(ipAddress)
                .userAgent(request.getUserAgent())
                .platform(request.getPlatform())
                .language(request.getLanguage())
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
        return successResponse(response);
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


}
