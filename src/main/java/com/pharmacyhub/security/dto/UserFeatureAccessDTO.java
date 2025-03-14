package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for user feature access information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeatureAccessDTO {
    private Long id;
    private String featureName;
    private String description;
    
    @Builder.Default
    private Map<String, Boolean> accessLevels = new HashMap<>();
    
    private boolean hasAccess;
    
    /**
     * Factory method to create a user feature access DTO
     */
    public static UserFeatureAccessDTO create(
            Long id, String featureName, String description, 
            Map<String, Boolean> accessLevels, boolean hasAccess) {
        return UserFeatureAccessDTO.builder()
                .id(id)
                .featureName(featureName)
                .description(description)
                .accessLevels(accessLevels != null ? accessLevels : new HashMap<>())
                .hasAccess(hasAccess)
                .build();
    }
}