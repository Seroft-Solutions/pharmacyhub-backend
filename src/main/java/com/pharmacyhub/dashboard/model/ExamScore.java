package com.pharmacyhub.dashboard.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * Exam Score model
 * 
 * Represents a user's score on an exam with comparative data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScore {
    private long id;
    private String name;
    private double score;
    private double average;
    private LocalDate date;
}