package com.pharmacyhub.controller;

import com.pharmacyhub.security.domain.Feature;
import com.pharmacyhub.security.dto.FeatureAccessDTO;
import com.pharmacyhub.security.service.FeatureService;
import com.pharmacyhub.security.service.RBACService;
import com.pharmacyhub.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for feature access-related endpoints
 * Provides centralized access control for frontend components
 */
@RestController
@RequestMapping("/api/feature-access")
@RequiredArgsConstructor
public class FeatureAccessController {
    private final FeatureService featureService;
    private final RBACService rbacService;
    private final SecurityUtils securityUtils;
    
    /**
     * Check if the current user has access to a specific feature
     * Returns detailed access information including allowed operations
     */
    @GetMapping("/check/{featureCode}")
    public ResponseEntity<FeatureAccessDTO> checkFeatureAccess(@PathVariable String featureCode) {
        Long userId = securityUtils.getCurrentUserId();
        Feature feature = featureService.getFeatureByCode(featureCode);
        
        boolean hasAccess = rbacService.userHasFeatureAccess(userId, featureCode);
        
        Set<String> allowedOperations = new HashSet<>();
        if (hasAccess && feature != null) {
            // Check each operation
            for (String operation : feature.getOperations()) {
                if (rbacService.userHasFeatureOperation(userId, featureCode, operation)) {
                    allowedOperations.add(operation);
                }
            }
        }
        
        FeatureAccessDTO accessDTO = FeatureAccessDTO.builder()
                .featureCode(featureCode)
                .name(feature != null ? feature.getName() : featureCode)
                .description(feature != null ? feature.getDescription() : "")
                .hasAccess(hasAccess)
                .allowedOperations(allowedOperations)
                .build();
        
        return ResponseEntity.ok(accessDTO);
    }
    
    /**
     * Check if the current user can perform a specific operation on a feature
     * Returns true if access is granted, false otherwise
     */
    @GetMapping("/check/{featureCode}/{operation}")
    public ResponseEntity<Boolean> checkOperationAccess(
            @PathVariable String featureCode, 
            @PathVariable String operation) {
        Long userId = securityUtils.getCurrentUserId();
        boolean hasAccess = rbacService.userHasFeatureOperation(userId, featureCode, operation);
        return ResponseEntity.ok(hasAccess);
    }
    
    /**
     * Get all features the current user has access to
     * Returns a list of features with access details
     */
    @GetMapping("/user-features")
    public ResponseEntity<List<FeatureAccessDTO>> getUserFeatures() {
        Long userId = securityUtils.getCurrentUserId();
        List<Feature> features = featureService.getAllFeatures();
        
        List<FeatureAccessDTO> accessDTOs = features.stream()
                .map(feature -> {
                    boolean hasAccess = rbacService.userHasFeatureAccess(userId, feature.getCode());
                    
                    Set<String> allowedOperations = new HashSet<>();
                    if (hasAccess) {
                        // Check each operation
                        for (String operation : feature.getOperations()) {
                            if (rbacService.userHasFeatureOperation(userId, feature.getCode(), operation)) {
                                allowedOperations.add(operation);
                            }
                        }
                    }
                    
                    return FeatureAccessDTO.builder()
                            .featureCode(feature.getCode())
                            .name(feature.getName())
                            .description(feature.getDescription())
                            .hasAccess(hasAccess)
                            .allowedOperations(allowedOperations)
                            .build();
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(accessDTOs);
    }
}
