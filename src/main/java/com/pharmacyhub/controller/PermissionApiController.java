package com.pharmacyhub.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.security.constants.ExamPermissionConstants;
import com.pharmacyhub.service.PermissionApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Controller for checking permissions for the frontend
 * Provides endpoints for the frontend application to check user permissions efficiently
 */
@RestController
@RequestMapping(APIConstants.BASE_MAPPING + "/permissions-api")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Permissions API", description = "API for checking user permissions and accessing permission information")
public class PermissionApiController {
    private static final Logger logger = LoggerFactory.getLogger(PermissionApiController.class);
    
    private final PermissionApiService permissionApiService;
    
    public PermissionApiController(PermissionApiService permissionApiService) {
        this.permissionApiService = permissionApiService;
    }
    
    /**
     * Check a single permission
     */
    @GetMapping("/check")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if the user has a specific permission")
    public ResponseEntity<ApiResponse<Boolean>> checkPermission(@RequestParam String permission) {
        logger.debug("Checking permission: {}", permission);
        boolean hasPermission = permissionApiService.hasPermission(permission);
        return ResponseEntity.ok(ApiResponse.success(hasPermission));
    }
    
    /**
     * Check multiple permissions at once
     */
    @PostMapping("/check-batch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check multiple permissions at once")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkPermissions(@RequestBody List<String> permissions) {
        logger.debug("Checking {} permissions", permissions.size());
        Map<String, Boolean> result = permissionApiService.checkMultiplePermissions(
                permissions.toArray(new String[0]));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Get all exam permissions for the current user
     */
    @GetMapping("/exams")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all exam-related permissions for the current user")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getExamPermissions() {
        logger.debug("Getting exam permissions");
        Map<String, Boolean> result = permissionApiService.checkExamPermissions();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Check if the user has admin privileges for exams
     */
    @GetMapping("/exams/admin")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if the user has admin privileges for exams")
    public ResponseEntity<ApiResponse<Boolean>> hasExamAdminPrivileges() {
        boolean hasAdminPrivileges = permissionApiService.hasExamAdminPrivileges();
        return ResponseEntity.ok(ApiResponse.success(hasAdminPrivileges));
    }
    
    /**
     * Get available exam permissions for documentation
     */
    @GetMapping("/exams/available")
    @Operation(summary = "Get a list of all available exam permissions")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableExamPermissions() {
        // This is just to document the available permissions, 
        // so the frontend knows what to check
        List<String> examPermissions = Arrays.asList(
                ExamPermissionConstants.VIEW_EXAMS,
                ExamPermissionConstants.TAKE_EXAM,
                ExamPermissionConstants.CREATE_EXAM,
                ExamPermissionConstants.EDIT_EXAM,
                ExamPermissionConstants.DELETE_EXAM,
                ExamPermissionConstants.DUPLICATE_EXAM,
                ExamPermissionConstants.MANAGE_QUESTIONS,
                ExamPermissionConstants.PUBLISH_EXAM,
                ExamPermissionConstants.UNPUBLISH_EXAM,
                ExamPermissionConstants.ASSIGN_EXAM,
                ExamPermissionConstants.GRADE_EXAM,
                ExamPermissionConstants.VIEW_RESULTS,
                ExamPermissionConstants.EXPORT_RESULTS,
                ExamPermissionConstants.VIEW_ANALYTICS
        );
        
        return ResponseEntity.ok(ApiResponse.success(examPermissions));
    }
}
