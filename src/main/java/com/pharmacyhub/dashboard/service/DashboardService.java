package com.pharmacyhub.dashboard.service;

import com.pharmacyhub.dashboard.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface for the Dashboard Service
 */
public interface DashboardService {
    /**
     * Get progress data for a user
     */
    UserProgress getUserProgress(String userId);
    
    /**
     * Get analytics data for a user
     */
    UserAnalytics getUserAnalytics(String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get personalized recommendations for a user
     */
    List<Recommendation> getUserRecommendations(String userId);
}