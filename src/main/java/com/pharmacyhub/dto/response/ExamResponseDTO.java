package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("durationMinutes")
    public Integer getDurationMinutes() {
        return duration;
    }
    
    private Integer totalMarks;
    private Integer passingMarks;
    private Exam.ExamStatus status;
    private List<String> tags = new ArrayList<>();
    private List<QuestionDTO> questions = new ArrayList<>();
    private Integer attemptCount;
    private Double averageScore;
    private String difficulty = "MEDIUM"; // Default difficulty for papers
    
    @JsonProperty("questionCount")
    public Integer getQuestionCount() {
        if (questions != null) {
            return questions.size();
        }
        return 0;
    }
    
    // For frontend compatibility - maps to tags
    @JsonProperty("topics_covered")
    public List<String> getTopicsCovered() {
        return tags;
    }
    
    // For frontend compatibility - calculated from duration
    @JsonProperty("time_limit")
    public Integer getTimeLimit() {
        return duration;
    }
    
    // For frontend compatibility
    @JsonProperty("premium")
    public Boolean isPremium() {
        return false; // Default non-premium
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuestionDTO {
        private Long id;
        private Integer questionNumber;
        private String text; // Changed from questionText to match frontend exact expectations
        private List<OptionDTO> options = new ArrayList<>();
        private String correctAnswer;
        private String explanation;
        private Integer marks;
        private String topic;
        private String difficulty;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionDTO {
        private Long id;
        private String label; // Updated from optionKey to match frontend
        private String text;  // Updated from optionText to match frontend
        private Boolean isCorrect;
    }
}
