package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO for the Feature entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureDTO {
    private Long id;
    private String name;
    private String description;
    private String code;
    
    @Builder.Default
    private boolean active = true;
    
    private Long parentFeatureId;
    
    @Builder.Default
    private List<String> permissions = new ArrayList<>();
    
    @Builder.Default
    private List<FeatureDTO> childFeatures = new ArrayList<>();
    
    @Builder.Default
    private Set<String> operations = new HashSet<>();
}
