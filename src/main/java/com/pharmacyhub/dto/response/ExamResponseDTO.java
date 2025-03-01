package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for exam data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamResponseDTO implements BaseDTO {
    
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private Integer totalMarks;
    private Integer passingMarks;
    private Exam.ExamStatus status;
    private List<QuestionDTO> questions = new ArrayList<>();
    private Integer attemptCount;
    private Double averageScore;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuestionDTO {
        private Long id;
        private Integer questionNumber;
        private String questionText;
        private List<OptionDTO> options = new ArrayList<>();
        private String correctAnswer;
        private String explanation;
        private Integer marks;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionDTO {
        private Long id;
        private String optionKey;
        private String optionText;
        private Boolean isCorrect;
    }
}
