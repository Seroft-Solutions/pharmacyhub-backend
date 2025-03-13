package com.pharmacyhub.security.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.security.annotation.RequiresPermission;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.service.PermissionDataLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for permission-related operations
 * Provides endpoints for retrieving permission data
 */
@RestController
@RequestMapping(APIConstants.BASE_MAPPING + "/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {
    private final PermissionDataLoaderService permissionDataLoaderService;

    /**
     * Get all permissions across the system
     * Only accessible to administrators
     */
    @GetMapping
    @RequiresPermission(resource = ResourceType.PERMISSION, operation = OperationType.READ)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllPermissions() {
        log.debug("Retrieving all permissions");
        
        List<Permission> permissions = permissionDataLoaderService.getAllPermissions();
        List<Map<String, Object>> result = permissions.stream()
                .map(this::mapPermissionToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Get all permissions grouped by resource type
     * Only accessible to administrators
     */
    @GetMapping("/structured")
    @RequiresPermission(resource = ResourceType.PERMISSION, operation = OperationType.READ)
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, Object>>>>> getPermissionStructure() {
        log.debug("Retrieving structured permissions");
        
        Map<String, List<Map<String, Object>>> structure = 
                permissionDataLoaderService.getPermissionStructure();
        
        return ResponseEntity.ok(ApiResponse.success(structure));
    }
    
    /**
     * Get all permissions for a specific feature
     * Only accessible to administrators
     */
    @GetMapping("/feature/{feature}")
    @RequiresPermission(resource = ResourceType.PERMISSION, operation = OperationType.READ)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFeaturePermissions(
            @PathVariable String feature) {
        log.debug("Retrieving permissions for feature: {}", feature);
        
        List<Permission> permissions = permissionDataLoaderService.getFeaturePermissions(feature);
        List<Map<String, Object>> result = permissions.stream()
                .map(this::mapPermissionToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Get all permissions related to exams
     * This is a convenience endpoint for the exam module
     */
    @GetMapping("/exams")
    @RequiresPermission(resource = ResourceType.PERMISSION, operation = OperationType.READ)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getExamPermissions() {
        log.debug("Retrieving exam permissions");
        
        List<Permission> permissions = permissionDataLoaderService.getExamPermissions();
        List<Map<String, Object>> result = permissions.stream()
                .map(this::mapPermissionToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * Reload and synchronize all permissions
     * This is an administrative function to ensure the system has the latest permissions
     */
    @PostMapping("/sync")
    @RequiresPermission(resource = ResourceType.PERMISSION, operation = OperationType.MANAGE)
    public ResponseEntity<ApiResponse<Void>> synchronizePermissions() {
        log.info("Synchronizing permissions");
        
        try {
            permissionDataLoaderService.synchronizePermissions();
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("Error synchronizing permissions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error synchronizing permissions: " + e.getMessage()));
        }
    }
    
    /**
     * Map Permission entity to DTO
     */
    private Map<String, Object> mapPermissionToDto(Permission permission) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", permission.getId());
        dto.put("name", permission.getName());
        dto.put("description", permission.getDescription());
        dto.put("resourceType", permission.getResourceType().name());
        dto.put("operationType", permission.getOperationType().name());
        dto.put("requiresApproval", permission.isRequiresApproval());
        return dto;
    }
}
