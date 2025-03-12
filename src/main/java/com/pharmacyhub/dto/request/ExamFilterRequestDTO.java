package com.pharmacyhub.dto.request;

import com.pharmacyhub.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for filtering exams
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamFilterRequestDTO implements BaseDTO {
    
    private String type; // MODEL, PAST, SUBJECT, PRACTICE
    private String difficulty; // EASY, MEDIUM, HARD
    private String topic; // Subject or category
    private Boolean isPremium;
    private Integer maxDuration;
    private String searchTerm;
}
