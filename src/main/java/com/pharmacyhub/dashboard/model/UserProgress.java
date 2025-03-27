package com.pharmacyhub.dashboard.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * User Progress model
 * 
 * Contains key progress indicators for the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {
    private int completedExams;
    private int inProgressExams;
    private double averageScore;
    private int totalTimeSpent; // in minutes
}