package com.pharmacyhub.security.service;

import com.pharmacyhub.security.domain.Feature;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.dto.FeatureDTO;
import com.pharmacyhub.security.exception.RBACException;
import com.pharmacyhub.security.infrastructure.FeatureRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FeatureService {
    private final FeatureRepository featureRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    /**
     * Get all features
     */
    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    /**
     * Get a feature by its code
     */
    public Feature getFeatureByCode(String code) {
        return featureRepository.findByCode(code)
                .orElseThrow(() -> RBACException.entityNotFound("Feature with code " + code));
    }

    /**
     * Create a new feature
     */
    @CacheEvict(value = {"featureAccess", "userFeatures"}, allEntries = true)
    public Feature createFeature(FeatureDTO featureDTO) {
        // Validate feature input
        if (featureDTO.getCode() == null || featureDTO.getCode().trim().isEmpty()) {
            throw RBACException.invalidInput("Feature code is required");
        }
        
        if (featureDTO.getName() == null || featureDTO.getName().trim().isEmpty()) {
            throw RBACException.invalidInput("Feature name is required");
        }
        
        // Check for duplicates
        if (featureRepository.findByCode(featureDTO.getCode()).isPresent()) {
            throw RBACException.duplicateEntity("Feature with code " + featureDTO.getCode() + " already exists");
        }
        
        // Create the feature
        Feature feature = Feature.builder()
                .name(featureDTO.getName())
                .description(featureDTO.getDescription())
                .code(featureDTO.getCode())
                .active(featureDTO.isActive())
                .build();
        
        // Set parent feature if provided
        if (featureDTO.getParentFeatureId() != null) {
            Feature parentFeature = featureRepository.findById(featureDTO.getParentFeatureId())
                    .orElseThrow(() -> RBACException.entityNotFound("Parent Feature"));
            feature.setParentFeature(parentFeature);
        }
        
        // Set permissions if provided
        if (featureDTO.getPermissions() != null && !featureDTO.getPermissions().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            
            for (String permissionName : featureDTO.getPermissions()) {
                Permission permission = permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> RBACException.entityNotFound("Permission " + permissionName));
                permissions.add(permission);
            }
            
            feature.setPermissions(permissions);
        }
        
        Feature savedFeature = featureRepository.save(feature);
        
        auditService.logSecurityEvent(
            "CREATE_FEATURE",
            String.format("Created feature '%s' with code '%s'", feature.getName(), feature.getCode()),
            "SUCCESS"
        );
        
        return savedFeature;
    }
    
    /**
     * Update an existing feature
     */
    @CacheEvict(value = {"featureAccess", "userFeatures"}, allEntries = true)
    public Feature updateFeature(Long featureId, FeatureDTO featureDTO) {
        Feature existingFeature = featureRepository.findById(featureId)
                .orElseThrow(() -> RBACException.entityNotFound("Feature"));
        
        // Update fields if provided
        if (featureDTO.getName() != null) {
            existingFeature.setName(featureDTO.getName());
        }
        
        if (featureDTO.getDescription() != null) {
            existingFeature.setDescription(featureDTO.getDescription());
        }
        
        if (featureDTO.getCode() != null) {
            // Check for duplicates if code is being changed
            if (!existingFeature.getCode().equals(featureDTO.getCode())) {
                if (featureRepository.findByCode(featureDTO.getCode()).isPresent()) {
                    throw RBACException.duplicateEntity("Feature with code " + featureDTO.getCode() + " already exists");
                }
            }
            existingFeature.setCode(featureDTO.getCode());
        }
        
        existingFeature.setActive(featureDTO.isActive());
        
        // Update parent feature if provided
        if (featureDTO.getParentFeatureId() != null) {
            if (featureDTO.getParentFeatureId().equals(featureId)) {
                throw RBACException.invalidInput("A feature cannot be its own parent");
            }
            
            Feature parentFeature = featureRepository.findById(featureDTO.getParentFeatureId())
                    .orElseThrow(() -> RBACException.entityNotFound("Parent Feature"));
            existingFeature.setParentFeature(parentFeature);
        } else if (featureDTO.getParentFeatureId() == null && existingFeature.getParentFeature() != null) {
            // Remove parent if null is explicitly provided
            existingFeature.setParentFeature(null);
        }
        
        // Update permissions if provided
        if (featureDTO.getPermissions() != null) {
            Set<Permission> permissions = new HashSet<>();
            
            for (String permissionName : featureDTO.getPermissions()) {
                Permission permission = permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> RBACException.entityNotFound("Permission " + permissionName));
                permissions.add(permission);
            }
            
            existingFeature.setPermissions(permissions);
        }
        
        Feature updatedFeature = featureRepository.save(existingFeature);
        
        auditService.logSecurityEvent(
            "UPDATE_FEATURE",
            String.format("Updated feature '%s' with code '%s'", updatedFeature.getName(), updatedFeature.getCode()),
            "SUCCESS"
        );
        
        return updatedFeature;
    }
    
    /**
     * Delete a feature
     */
    @CacheEvict(value = {"featureAccess", "userFeatures"}, allEntries = true)
    public void deleteFeature(Long featureId) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> RBACException.entityNotFound("Feature"));
        
        // Check if feature has child features
        if (!feature.getChildFeatures().isEmpty()) {
            throw RBACException.invalidOperation("Cannot delete feature with child features");
        }
        
        featureRepository.delete(feature);
        
        auditService.logSecurityEvent(
            "DELETE_FEATURE",
            String.format("Deleted feature '%s' with code '%s'", feature.getName(), feature.getCode()),
            "SUCCESS"
        );
    }
    
    /**
     * Convert Feature to FeatureDTO
     */
    public FeatureDTO convertFeatureToDTO(Feature feature) {
        FeatureDTO dto = FeatureDTO.builder()
                .id(feature.getId())
                .name(feature.getName())
                .description(feature.getDescription())
                .code(feature.getCode())
                .active(feature.isActive())
                .parentFeatureId(feature.getParentFeature() != null ? feature.getParentFeature().getId() : null)
                .permissions(feature.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toList()))
                .build();
        
        // Add child features if any
        if (feature.getChildFeatures() != null && !feature.getChildFeatures().isEmpty()) {
            List<FeatureDTO> childFeatureDTOs = feature.getChildFeatures().stream()
                    .map(this::convertFeatureToDTO)
                    .collect(Collectors.toList());
            dto.setChildFeatures(childFeatureDTOs);
        }
        
        return dto;
    }
    
    /**
     * Get all permissions for a feature, including those from parent features
     */
    public Set<Permission> getAllFeaturePermissions(Feature feature) {
        Set<Permission> allPermissions = new HashSet<>(feature.getPermissions());
        
        // Add permissions from parent feature recursively
        Feature parentFeature = feature.getParentFeature();
        if (parentFeature != null) {
            allPermissions.addAll(getAllFeaturePermissions(parentFeature));
        }
        
        return allPermissions;
    }
}