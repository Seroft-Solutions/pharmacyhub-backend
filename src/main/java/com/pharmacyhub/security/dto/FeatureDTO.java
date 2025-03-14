package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating and updating features
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureDTO {
    private Long id;
    private String name;
    private String description;
    
    @Builder.Default
    private List<FeaturePermissionDTO> permissions = new ArrayList<>();
}