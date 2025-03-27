package com.pharmacyhub.dashboard.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Study Hours model
 * 
 * Represents the number of hours a user studied on a specific day.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyHours {
    private String date;
    private double hours;
}