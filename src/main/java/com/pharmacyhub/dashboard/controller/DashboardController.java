package com.pharmacyhub.dashboard.controller;

import com.pharmacyhub.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pharmacyhub.dashboard.model.*;
import com.pharmacyhub.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard Controller
 * 
 * Provides endpoints for dashboard data related to user progress, analytics, and recommendations.
 */
@RestController
@RequestMapping("/api")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get user progress data
     */
    @GetMapping("/progress/{userId}")
    public ResponseEntity<ApiResponse<UserProgress>> getUserProgress(@PathVariable String userId) {
        UserProgress progress = dashboardService.getUserProgress(userId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * Get user analytics data with optional date filters
     */
    @GetMapping("/analytics/{userId}")
    public ResponseEntity<ApiResponse<UserAnalytics>> getUserAnalytics(
            @PathVariable String userId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        UserAnalytics analytics = dashboardService.getUserAnalytics(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    /**
     * Get user recommendations
     */
    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<ApiResponse<List<Recommendation>>> getUserRecommendations(
            @PathVariable String userId) {
        
        List<Recommendation> recommendations = dashboardService.getUserRecommendations(userId);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
}