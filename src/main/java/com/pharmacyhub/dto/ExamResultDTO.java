package com.pharmacyhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultDTO {
    
    private Long examId;
    private String examTitle;
    private Double score;
    private Integer totalMarks;
    private Integer passingMarks;
    private Boolean isPassed;
    private Integer timeSpent;
    private List<QuestionResultDTO> questionResults;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResultDTO {
        private Long questionId;
        private String questionText;
        private String userAnswerId;
        private String correctAnswerId;
        private Boolean isCorrect;
        private String explanation;
        private Integer points;
        private Integer earnedPoints;
    }
}
