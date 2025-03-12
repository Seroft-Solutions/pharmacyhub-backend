package com.pharmacyhub.dto.request;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.dto.BaseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for creating or updating an exam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamRequestDTO implements BaseDTO {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;
    
    @NotNull(message = "Total marks is required")
    @Min(value = 1, message = "Total marks must be at least 1")
    private Integer totalMarks;
    
    @NotNull(message = "Passing marks is required")
    @Min(value = 0, message = "Passing marks cannot be negative")
    private Integer passingMarks;
    
    private Exam.ExamStatus status = Exam.ExamStatus.DRAFT;
    
    private List<String> tags = new ArrayList<>();
    
    @Valid
    private List<QuestionDTO> questions = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDTO {
        private Long id;
        
        @NotNull(message = "Question number is required")
        private Integer questionNumber;
        
        @NotBlank(message = "Question text is required")
        private String questionText;
        
        @Valid
        private List<OptionDTO> options = new ArrayList<>();
        
        private String correctAnswer;
        private String explanation;
        
        @NotNull(message = "Marks value is required")
        @Min(value = 1, message = "Marks must be at least 1")
        private Integer marks;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        private Long id;
        
        @NotBlank(message = "Option key is required")
        private String optionKey;
        
        @NotBlank(message = "Option text is required")
        private String optionText;
        
        private Boolean isCorrect = false;
    }
}
