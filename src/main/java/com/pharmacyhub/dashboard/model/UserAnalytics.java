package com.pharmacyhub.dashboard.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * User Analytics model
 * 
 * Contains detailed analytics data for a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalytics {
    private List<ExamScore> examScores;
    private List<StudyHours> studyHours;
    private Map<String, Integer> timeSpent; // subject -> minutes
}