package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * DTO for feature access information
 * Contains information about a feature and the operations a user can perform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureAccessDTO {
    private String featureCode;
    private String name;
    private String description;
    private boolean hasAccess;
    
    @Builder.Default
    private Set<String> allowedOperations = new HashSet<>();
}
