package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating and updating feature permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturePermissionDTO {
    private Long id;
    private String accessLevel;
    
    @Builder.Default
    private List<Long> permissionIds = new ArrayList<>();
}