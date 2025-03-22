package com.pharmacyhub.dto.response;

import com.pharmacyhub.domain.entity.Exam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for exam responses")
public class ExamResponseDTO {
    @Schema(description = "Unique identifier for the exam", example = "1")
    private Long id;
    
    @Schema(description = "Title of the exam", example = "Pharmacology Final Exam")
    private String title;
    
    @Schema(description = "Detailed description of the exam", example = "Final examination covering all pharmacology topics for the semester")
    private String description;
    
    @Schema(description = "Duration of the exam in minutes", example = "60")
    private Integer duration;
    
    @Schema(description = "Total marks for the exam", example = "100")
    private Integer totalMarks;
    
    @Schema(description = "Passing marks for the exam", example = "60")
    private Integer passingMarks;
    
    @Schema(description = "Status of the exam", example = "PUBLISHED", allowableValues = {"DRAFT", "PUBLISHED", "ARCHIVED"})
    private Exam.ExamStatus status;
    
    @Schema(description = "Number of questions in the exam", example = "20")
    private Integer questionCount;
    
    @Schema(description = "Number of attempts made for this exam by all users", example = "150")
    private Integer attemptCount;
    
    @Schema(description = "Average score achieved by users in this exam", example = "78.5")
    private Double averageScore;
    
    @ArraySchema(schema = @Schema(description = "Tag associated with the exam", example = "PRACTICE"))
    @Schema(description = "List of tags associated with the exam")
    private List<String> tags;
    
    @ArraySchema(schema = @Schema(implementation = QuestionDTO.class))
    @Schema(description = "List of questions in the exam")
    private List<QuestionDTO> questions;
    
    @Schema(description = "Difficulty level of the exam", example = "MEDIUM", allowableValues = {"EASY", "MEDIUM", "HARD"})
    private String difficulty;
    
    // Premium exam fields
    @Schema(description = "Flag indicating if the exam is premium", example = "true")
    private boolean premium;
    
    @Schema(description = "Price of the premium exam", example = "2000")
    private BigDecimal price;
    
    @Schema(description = "Flag indicating if the user has purchased this exam", example = "false")
    private boolean purchased;
    
    @Schema(description = "Flag indicating if the exam has a custom price", example = "false")
    private boolean customPrice;
    
    // Nested class for Question DTOs
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Data transfer object for questions in an exam response")
    public static class QuestionDTO {
        @Schema(description = "Unique identifier for the question", example = "1")
        private Long id;
        
        @Schema(description = "Number of the question in the exam", example = "1")
        private Integer questionNumber;
        
        @Schema(description = "Text content of the question", example = "What is the primary action of Aspirin?")
        private String text;
        
        @Schema(description = "Correct answer identifier (e.g., A, B, C, D)", example = "A")
        private String correctAnswer;
        
        @Schema(description = "Explanation for the correct answer", example = "Aspirin works as an anti-inflammatory by inhibiting COX enzymes.")
        private String explanation;
        
        @Schema(description = "Marks awarded for the question", example = "1")
        private Integer marks;
        
        @ArraySchema(schema = @Schema(implementation = OptionDTO.class))
        @Schema(description = "List of options for the question")
        private List<OptionDTO> options;
        
        @Schema(description = "Topic category for the question", example = "Pharmacokinetics")
        private String topic;
        
        @Schema(description = "Difficulty level of the question", example = "MEDIUM", allowableValues = {"EASY", "MEDIUM", "HARD"})
        private String difficulty;
    }
    
    // Nested class for Option DTOs
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Data transfer object for options in a question response")
    public static class OptionDTO {
        @Schema(description = "Unique identifier for the option", example = "1")
        private Long id;
        
        @Schema(description = "Key/label for the option (e.g., A, B, C, D)", example = "A")
        private String label;
        
        @Schema(description = "Text content of the option", example = "It prevents blood clotting")
        private String text;
        
        @Schema(description = "Flag indicating if this option is correct", example = "true")
        private Boolean isCorrect;
    }
}