package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pharmacyhub.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for question data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponseDTO implements BaseDTO {
    
    private Long id;
    private Integer questionNumber;
    private String text; // Renamed from questionText to match frontend expectations
    @Builder.Default
    private List<OptionDTO> options = new ArrayList<>();
    private String explanation;
    private Integer points;
    private String topic;
    private String difficulty;
    private String correctAnswer;

    
    // For frontend compatibility - maps points to marks
    @JsonProperty("marks")
    public Integer getMarks() {
        return points;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionDTO {
        private Long id;
        private String label;
        private String text;
    }
}
