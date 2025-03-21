package com.pharmacyhub.dto.response;

import com.pharmacyhub.domain.entity.Exam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private Integer totalMarks;
    private Integer passingMarks;
    private Exam.ExamStatus status;
    private Integer questionCount;
    private Integer attemptCount;
    private Double averageScore;
    private List<String> tags;
    private List<QuestionDTO> questions;
    private String difficulty;
    
    // Premium exam fields
    private boolean premium;
    private BigDecimal price;
    private boolean purchased;
    private boolean customPrice;
    
    // Nested class for Question DTOs
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDTO {
        private Long id;
        private Integer questionNumber;
        private String text;
        private String correctAnswer;
        private String explanation;
        private Integer marks;
        private List<OptionDTO> options;
        private String topic;
        private String difficulty;
    }
    
    // Nested class for Option DTOs
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        private Long id;
        private String label;
        private String text;
        private Boolean isCorrect;
    }
}