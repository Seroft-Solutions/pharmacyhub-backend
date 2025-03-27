package com.pharmacyhub.controller.session;

import com.pharmacyhub.controller.base.BaseController;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.session.*;
import com.pharmacyhub.exception.SessionNotFoundException;
import com.pharmacyhub.exception.SessionValidationException;
import com.pharmacyhub.service.session.SessionManagementService;
import com.pharmacyhub.service.session.SessionOtpService;
import com.pharmacyhub.service.session.SessionValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for session management
 */
@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Session Management", description = "API endpoints for session management and validation")
@RequiredArgsConstructor
public class SessionController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    
    private final SessionValidationService sessionValidationService;
    private final SessionManagementService sessionManagementService;
    private final SessionOtpService sessionOtpService;
    private final UserRepository userRepository;
    
    /**
     * Validate login attempt and check for suspicious activity
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate login attempt and check for suspicious activity")
    public ResponseEntity<ApiResponse<LoginValidationResultDTO>> validateLogin(
            @Valid @RequestBody LoginValidationRequestDTO request,
            HttpServletRequest servletRequest) {
        
        // If IP address not provided, get it from request
        if (request.getIpAddress() == null || request.getIpAddress().isBlank()) {
            String ipAddress = getClientIpAddress(servletRequest);
            request.setIpAddress(ipAddress);
            logger.debug("Setting client IP: {}", ipAddress);
        }
        
        try {
            // Call validation service - may throw SessionValidationException
            LoginValidationResultDTO result = sessionValidationService.validateLogin(request);
            return successResponse(result);
        } catch (SessionValidationException ex) {
            // If it's an actual error like TOO_MANY_DEVICES, it will be handled by the
            // exception handler and this code won't be reached
            throw ex;
        } catch (Exception ex) {
            // Handle other exceptions
            logger.error("Error validating login: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
    /**
     * Get all sessions for current user
     */
    @GetMapping("/me")
    @Operation(summary = "Get all sessions for current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LoginSessionDTO>>> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        // Extract user ID from UserDetails
        Long userId = getUserIdFromUserDetails(userDetails);
        
        // Create filter criteria
        SessionFilterCriteriaDTO criteria = SessionFilterCriteriaDTO.builder()
            .active(active)
            .fromDate(fromDate)
            .toDate(toDate)
            .build();
        
        List<LoginSessionDTO> sessions = sessionManagementService.getUserSessions(userId, criteria);
        return successResponse(sessions);
    }
    
    /**
     * Get all sessions for a specific user
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get all sessions for a specific user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoginSessionDTO>>> getUserSessions(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean suspicious,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        // Create filter criteria
        SessionFilterCriteriaDTO criteria = SessionFilterCriteriaDTO.builder()
            .active(active)
            .suspicious(suspicious)
            .fromDate(fromDate)
            .toDate(toDate)
            .build();
        
        List<LoginSessionDTO> sessions = sessionManagementService.getUserSessions(userId, criteria);
        return successResponse(sessions);
    }
    
    /**
     * Get a specific session
     */
    @GetMapping("/{sessionId}")
    @Operation(summary = "Get a specific session")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoginSessionDTO>> getSession(@PathVariable UUID sessionId) {
        LoginSessionDTO session = sessionManagementService.getSession(sessionId);
        return successResponse(session);
    }
    
    /**
     * Terminate a session
     */
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Terminate a session")
    @PreAuthorize("hasRole('ADMIN') or @sessionAuthorizationEvaluator.isOwnSession(#sessionId, authentication.principal)")
    public ResponseEntity<ApiResponse<String>> terminateSession(@PathVariable UUID sessionId) {
        sessionManagementService.terminateSession(sessionId);
        return successResponse("Session terminated successfully");
    }
    
    /**
     * Terminate all other sessions for a user
     */
    @PostMapping("/users/{userId}/terminate-others")
    @Operation(summary = "Terminate all other sessions for a user")
    @PreAuthorize("hasRole('ADMIN') or #userId == @sessionAuthorizationEvaluator.getUserId(authentication.principal)")
    public ResponseEntity<ApiResponse<String>> terminateOtherSessions(
            @PathVariable Long userId,
            @Valid @RequestBody TerminateOtherSessionsRequestDTO request) {
        
        try {
            sessionManagementService.terminateOtherSessions(userId, request.getCurrentSessionId());
            return successResponse("Other sessions terminated successfully");
        } catch (SessionNotFoundException ex) {
            // Let the exception handler take care of it
            throw ex;
        } catch (Exception ex) {
            logger.error("Error terminating other sessions: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
    
    /**
     * Require OTP verification for a user's next login
     */
    @PostMapping("/users/{userId}/require-otp")
    @Operation(summary = "Require OTP verification for a user's next login")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> requireOtpVerification(@PathVariable Long userId) {
        sessionManagementService.requireOtpVerification(userId);
        return successResponse("OTP verification required for next login");
    }
    
    /**
     * Verify OTP
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP")
    public ResponseEntity<ApiResponse<OtpVerificationResponseDTO>> verifyOtp(
            @Valid @RequestBody OtpVerificationRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Extract user ID from UserDetails
        Long userId = getUserIdFromUserDetails(userDetails);
        
        // Parse session ID from request
        UUID sessionId = UUID.fromString(request.getSessionId());
        
        OtpVerificationResponseDTO response = sessionOtpService.verifyOtp(userId, sessionId, request.getOtp());
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
    
    /**
     * Get user ID from UserDetails
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // This will depend on your UserDetails implementation
        // For now, assuming it's the User entity that has an getId() method
        if (userDetails instanceof com.pharmacyhub.entity.User) {
            return ((com.pharmacyhub.entity.User) userDetails).getId();
        }
        
        // Fallback using username
        return userRepository.findByEmailAddress(userDetails.getUsername())
            .map(User::getId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
