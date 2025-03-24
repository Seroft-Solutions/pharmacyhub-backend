package com.pharmacyhub.controller.session;

import com.pharmacyhub.controller.base.BaseController;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.session.LoginSessionDTO;
import com.pharmacyhub.dto.session.SessionFilterCriteriaDTO;
import com.pharmacyhub.service.session.SessionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for admin session monitoring
 */
@RestController
@RequestMapping("/api/v1/sessions/monitoring")
@Tag(name = "Session Monitoring", description = "API endpoints for admin session monitoring")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSessionController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminSessionController.class);
    
    private final SessionManagementService sessionManagementService;
    
    /**
     * Get all sessions with filtering options
     */
    @GetMapping
    @Operation(summary = "Get all sessions with filtering options")
    public ResponseEntity<ApiResponse<List<LoginSessionDTO>>> getAllSessions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean suspicious,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String country) {
        
        // Create filter criteria
        SessionFilterCriteriaDTO criteria = SessionFilterCriteriaDTO.builder()
            .userId(userId)
            .active(active)
            .suspicious(suspicious)
            .fromDate(fromDate)
            .toDate(toDate)
            .country(country)
            .build();
        
        List<LoginSessionDTO> sessions = sessionManagementService.getAllSessions(criteria);
        return successResponse(sessions);
    }
    
    /**
     * Get suspicious sessions (multiple locations)
     */
    @GetMapping("/suspicious")
    @Operation(summary = "Get suspicious sessions (multiple locations)")
    public ResponseEntity<ApiResponse<List<LoginSessionDTO>>> getSuspiciousSessions() {
        List<LoginSessionDTO> sessions = sessionManagementService.getSuspiciousSessions();
        return successResponse(sessions);
    }
    
    /**
     * Get sessions requiring OTP verification
     */
    @GetMapping("/requiring-otp")
    @Operation(summary = "Get sessions requiring OTP verification")
    public ResponseEntity<ApiResponse<List<LoginSessionDTO>>> getSessionsRequiringOtp() {
        SessionFilterCriteriaDTO criteria = SessionFilterCriteriaDTO.builder()
            .requiresOtp(true)
            .build();
        
        List<LoginSessionDTO> sessions = sessionManagementService.getAllSessions(criteria);
        return successResponse(sessions);
    }
    
    /**
     * Get sessions from a specific country
     */
    @GetMapping("/countries/{country}")
    @Operation(summary = "Get sessions from a specific country")
    public ResponseEntity<ApiResponse<List<LoginSessionDTO>>> getSessionsByCountry(@PathVariable String country) {
        SessionFilterCriteriaDTO criteria = SessionFilterCriteriaDTO.builder()
            .country(country)
            .build();
        
        List<LoginSessionDTO> sessions = sessionManagementService.getAllSessions(criteria);
        return successResponse(sessions);
    }
}
