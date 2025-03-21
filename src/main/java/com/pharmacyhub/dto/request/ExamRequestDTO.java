package com.pharmacyhub.dto.request;

import com.pharmacyhub.domain.entity.Exam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamRequestDTO {
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be positive or zero")
    private Integer duration;
    
    @NotNull(message = "Total marks is required")
    @PositiveOrZero(message = "Total marks must be positive or zero")
    private Integer totalMarks;
    
    @NotNull(message = "Passing marks is required")
    @PositiveOrZero(message = "Passing marks must be positive or zero")
    private Integer passingMarks;
    
    private Exam.ExamStatus status;
    
    private List<String> tags;
    
    private List<QuestionDTO> questions;
    
    // Premium exam fields
    private Boolean isPremium;
    private BigDecimal price;
    private Boolean isCustomPrice;
    
    // Nested class for Question DTOs
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDTO {
        private Long id;
        private Integer questionNumber;
        private String questionText;
        private String correctAnswer;
        private String explanation;
        private Integer marks;
        private List<OptionDTO> options;
    }
    
    // Nested class for Option DTOs
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        private Long id;
        private String optionKey;
        private String optionText;
        private Boolean isCorrect;
    }
}