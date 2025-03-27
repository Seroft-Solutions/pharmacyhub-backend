package com.pharmacyhub.dashboard.service;

import com.pharmacyhub.dashboard.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Implementation of the Dashboard Service
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    /**
     * Get progress data for a user
     */
    @Override
    public UserProgress getUserProgress(String userId) {
        // Mock implementation - replace with real implementation later
        return UserProgress.builder()
                .completedExams(12)
                .inProgressExams(3)
                .averageScore(74.5)
                .totalTimeSpent(1860) // in minutes (31 hours)
                .build();
    }
    
    /**
     * Get analytics data for a user
     */
    @Override
    public UserAnalytics getUserAnalytics(String userId, LocalDate startDate, LocalDate endDate) {
        // Mock implementation - replace with real implementation later
        
        // Create sample exam scores
        List<ExamScore> examScores = Arrays.asList(
            ExamScore.builder().id(1).name("Pharmacology Basics").score(85).average(72).date(LocalDate.of(2025, 3, 1)).build(),
            ExamScore.builder().id(2).name("Drug Interactions").score(78).average(68).date(LocalDate.of(2025, 3, 5)).build(),
            ExamScore.builder().id(3).name("Pharmaceutical Chemistry").score(92).average(75).date(LocalDate.of(2025, 3, 10)).build(),
            ExamScore.builder().id(4).name("Clinical Pharmacy").score(65).average(62).date(LocalDate.of(2025, 3, 15)).build(),
            ExamScore.builder().id(5).name("Pharmacy Law").score(88).average(70).date(LocalDate.of(2025, 3, 20)).build()
        );
        
        // Create study hours for each day of the week
        List<StudyHours> studyHours = Arrays.asList(
            StudyHours.builder().date("Mon").hours(2.5).build(),
            StudyHours.builder().date("Tue").hours(1.8).build(),
            StudyHours.builder().date("Wed").hours(3.2).build(),
            StudyHours.builder().date("Thu").hours(1.5).build(),
            StudyHours.builder().date("Fri").hours(2.0).build(),
            StudyHours.builder().date("Sat").hours(4.5).build(),
            StudyHours.builder().date("Sun").hours(3.7).build()
        );
        
        // Create time spent breakdown by subject
        Map<String, Integer> timeSpent = new HashMap<>();
        timeSpent.put("Pharmacology", 450); // minutes
        timeSpent.put("Chemistry", 360);
        timeSpent.put("Biology", 240);
        timeSpent.put("Physiology", 180);
        timeSpent.put("Pathology", 210);
        timeSpent.put("Pharmacy Practice", 420);
        
        return UserAnalytics.builder()
                .examScores(examScores)
                .studyHours(studyHours)
                .timeSpent(timeSpent)
                .build();
    }
    
    /**
     * Get personalized recommendations for a user
     */
    @Override
    public List<Recommendation> getUserRecommendations(String userId) {
        // Mock implementation - replace with real implementation later
        return Arrays.asList(
            Recommendation.builder()
                .id("1")
                .title("Pharmacokinetics - Advanced Concepts")
                .type(Recommendation.RecommendationType.EXAM)
                .confidence(0.92)
                .tags(Arrays.asList("pharmacology", "advanced", "kinetics"))
                .build(),
            Recommendation.builder()
                .id("2")
                .title("Drug Interactions in Clinical Practice")
                .type(Recommendation.RecommendationType.COURSE)
                .confidence(0.87)
                .tags(Arrays.asList("clinical", "interactions", "practice"))
                .build(),
            Recommendation.builder()
                .id("3")
                .title("Pharmaceutical Calculations Refresher")
                .type(Recommendation.RecommendationType.EXAM)
                .confidence(0.78)
                .tags(Arrays.asList("calculations", "basic", "practice"))
                .build(),
            Recommendation.builder()
                .id("4")
                .title("Hospital Pharmacy Guidelines")
                .type(Recommendation.RecommendationType.RESOURCE)
                .confidence(0.65)
                .tags(Arrays.asList("hospital", "guidelines", "practice"))
                .build()
        );
    }
}