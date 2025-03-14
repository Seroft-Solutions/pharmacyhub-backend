package com.pharmacyhub.security.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.annotation.RequiresPermission;
import com.pharmacyhub.security.domain.Feature;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.dto.FeatureDTO;
import com.pharmacyhub.security.service.FeatureService;
import com.pharmacyhub.security.service.RBACService;
import com.pharmacyhub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for feature-based access control operations
 */
@RestController
@RequestMapping(APIConstants.BASE_MAPPING + "/features")
@RequiredArgsConstructor
@Slf4j
public class FeatureController {
    private final RBACService rbacService;
    private final FeatureService featureService;
    private final UserService userService;

    /**
     * Get all features accessible to the current user
     */
    @GetMapping("/accessible")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FeatureDTO>>> getAccessibleFeatures() {
        User currentUser = userService.getLoggedInUser();
        log.debug("Getting accessible features for user ID: {}", currentUser.getId());
        
        Set<Feature> features = rbacService.getUserAccessibleFeatures(currentUser.getId());
        List<FeatureDTO> featureDTOs = features.stream()
                .map(featureService::convertFeatureToDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(featureDTOs));
    }
    
    /**
     * Check if the current user has access to a specific feature
     */
    @GetMapping("/check-access/{featureCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> checkFeatureAccess(
            @PathVariable String featureCode) {
        User currentUser = userService.getLoggedInUser();
        log.debug("Checking access to feature '{}' for user ID: {}", featureCode, currentUser.getId());
        
        boolean hasAccess = rbacService.userHasFeatureAccess(currentUser.getId(), featureCode);
        return ResponseEntity.ok(ApiResponse.success(hasAccess));
    }
    
    /**
     * Check if the current user has access to multiple features at once
     */
    @PostMapping("/check-bulk-access")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkBulkFeatureAccess(
            @RequestBody List<String> featureCodes) {
        User currentUser = userService.getLoggedInUser();
        log.debug("Checking bulk access to features for user ID: {}", currentUser.getId());
        
        Map<String, Boolean> results = new HashMap<>();
        
        for (String featureCode : featureCodes) {
            boolean hasAccess = rbacService.userHasFeatureAccess(currentUser.getId(), featureCode);
            results.put(featureCode, hasAccess);
        }
        
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    
    /**
     * Get all available features in the system (admin only)
     */
    @GetMapping
    @RequiresPermission(resource = ResourceType.FEATURE, operation = OperationType.READ)
    public ResponseEntity<ApiResponse<List<FeatureDTO>>> getAllFeatures() {
        log.debug("Getting all features");
        
        List<Feature> features = featureService.getAllFeatures();
        List<FeatureDTO> featureDTOs = features.stream()
                .map(featureService::convertFeatureToDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success(featureDTOs));
    }
    
    /**
     * Create a new feature (admin only)
     */
    @PostMapping
    @RequiresPermission(resource = ResourceType.FEATURE, operation = OperationType.CREATE)
    public ResponseEntity<ApiResponse<FeatureDTO>> createFeature(@RequestBody FeatureDTO featureDTO) {
        log.debug("Creating new feature: {}", featureDTO.getCode());
        
        Feature feature = featureService.createFeature(featureDTO);
        FeatureDTO createdFeatureDTO = featureService.convertFeatureToDTO(feature);
        
        return ResponseEntity.ok(ApiResponse.success(createdFeatureDTO));
    }
    
    /**
     * Update an existing feature (admin only)
     */
    @PutMapping("/{id}")
    @RequiresPermission(resource = ResourceType.FEATURE, operation = OperationType.UPDATE)
    public ResponseEntity<ApiResponse<FeatureDTO>> updateFeature(
            @PathVariable Long id, @RequestBody FeatureDTO featureDTO) {
        log.debug("Updating feature ID: {}", id);
        
        Feature feature = featureService.updateFeature(id, featureDTO);
        FeatureDTO updatedFeatureDTO = featureService.convertFeatureToDTO(feature);
        
        return ResponseEntity.ok(ApiResponse.success(updatedFeatureDTO));
    }
    
    /**
     * Delete a feature (admin only)
     */
    @DeleteMapping("/{id}")
    @RequiresPermission(resource = ResourceType.FEATURE, operation = OperationType.DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteFeature(@PathVariable Long id) {
        log.debug("Deleting feature ID: {}", id);
        
        featureService.deleteFeature(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}