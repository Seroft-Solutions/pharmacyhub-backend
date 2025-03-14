package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for feature access checks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureAccessResponse {
    private boolean granted;
    private String message;
    private Long featureId;
    private String featureName;
    private String accessLevel;

    /**
     * Factory method for creating granted responses
     */
    public static FeatureAccessResponse granted(String message) {
        return FeatureAccessResponse.builder()
                .granted(true)
                .message(message)
                .build();
    }

    /**
     * Factory method for creating denied responses
     */
    public static FeatureAccessResponse denied(String message) {
        return FeatureAccessResponse.builder()
                .granted(false)
                .message(message)
                .build();
    }
    
    /**
     * Factory method for creating detailed granted responses
     */
    public static FeatureAccessResponse grantedWithDetails(
            String message, Long featureId, String featureName, String accessLevel) {
        return FeatureAccessResponse.builder()
                .granted(true)
                .message(message)
                .featureId(featureId)
                .featureName(featureName)
                .accessLevel(accessLevel)
                .build();
    }
    
    /**
     * Factory method for creating detailed denied responses
     */
    public static FeatureAccessResponse deniedWithDetails(
            String message, Long featureId, String featureName, String accessLevel) {
        return FeatureAccessResponse.builder()
                .granted(false)
                .message(message)
                .featureId(featureId)
                .featureName(featureName)
                .accessLevel(accessLevel)
                .build();
    }
}