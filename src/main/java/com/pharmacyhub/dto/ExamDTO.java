package com.pharmacyhub.dto;

import com.pharmacyhub.domain.entity.Exam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private Integer totalMarks;
    private Integer passingMarks;
    private Exam.ExamStatus status;
    private List<QuestionDTO> questions = new ArrayList<>();
    private boolean premium;
    private BigDecimal price;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDTO {
        private Long id;
        private Integer questionNumber;
        private String questionText;
        private List<OptionDTO> options = new ArrayList<>();
        private String correctAnswer;
        private String explanation;
        private Integer marks;
    }
    
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