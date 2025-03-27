package com.pharmacyhub.dashboard.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Recommendation model
 * 
 * Represents a personalized recommendation for a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private String id;
    private String title;
    private RecommendationType type;
    private double confidence;
    private List<String> tags;
    
    /**
     * Type of recommendation
     */
    public enum RecommendationType {
        EXAM,
        COURSE,
        RESOURCE
    }
}