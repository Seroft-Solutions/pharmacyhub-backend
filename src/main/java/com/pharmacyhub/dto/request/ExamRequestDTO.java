package com.pharmacyhub.dto.request;

import com.pharmacyhub.domain.entity.Exam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for exam creation and update requests")
public class ExamRequestDTO {
    @Schema(description = "Unique identifier for the exam", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Schema(description = "Title of the exam", example = "Pharmacology Final Exam", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @Schema(description = "Detailed description of the exam", example = "Final examination covering all pharmacology topics for the semester")
    private String description;
    
    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be positive or zero")
    @Schema(description = "Duration of the exam in minutes", example = "60", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;
    
    @NotNull(message = "Total marks is required")
    @PositiveOrZero(message = "Total marks must be positive or zero")
    @Schema(description = "Total marks for the exam", example = "100", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalMarks;
    
    @NotNull(message = "Passing marks is required")
    @PositiveOrZero(message = "Passing marks must be positive or zero")
    @Schema(description = "Passing marks for the exam", example = "60", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer passingMarks;
    
    @Schema(description = "Status of the exam", example = "PUBLISHED", allowableValues = {"DRAFT", "PUBLISHED", "ARCHIVED"})
    private Exam.ExamStatus status;
    
    @ArraySchema(schema = @Schema(description = "Tag associated with the exam", example = "PRACTICE"))
    @Schema(description = "List of tags associated with the exam")
    private List<String> tags;
    
    @ArraySchema(schema = @Schema(implementation = QuestionDTO.class))
    @Schema(description = "List of questions in the exam")
    private List<QuestionDTO> questions;
    
    // Premium exam fields
    @Schema(description = "Flag indicating if the exam is premium", example = "true")
    private Boolean isPremium;
    
    @Schema(description = "Price of the premium exam", example = "2000")
    private BigDecimal price;
    
    @Schema(description = "Flag indicating if the exam has a custom price", example = "false")
    private Boolean isCustomPrice;
    
    // Nested class for Question DTOs
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Data transfer object for questions in an exam")
    public static class QuestionDTO {
        @Schema(description = "Unique identifier for the question", example = "1")
        private Long id;
        
        @Schema(description = "Number of the question in the exam", example = "1")
        private Integer questionNumber;
        
        @Schema(description = "Text content of the question", example = "What is the primary action of Aspirin?")
        private String questionText;
        
        @Schema(description = "Correct answer identifier (e.g., A, B, C, D)", example = "A")
        private String correctAnswer;
        
        @Schema(description = "Explanation for the correct answer", example = "Aspirin works as an anti-inflammatory by inhibiting COX enzymes.")
        private String explanation;
        
        @Schema(description = "Marks awarded for the question", example = "1")
        private Integer marks;
        
        @ArraySchema(schema = @Schema(implementation = OptionDTO.class))
        @Schema(description = "List of options for the question")
        private List<OptionDTO> options;
        
        // Additional setter to handle 'text' field from frontend
        @Schema(hidden = true) // Hide from OpenAPI documentation as it's just for compatibility
        public void setText(String text) {
            this.questionText = text;
        }
        
        // Getter for frontend compatibility
        @Schema(description = "Alias for questionText (for frontend compatibility)", example = "What is the primary action of Aspirin?")
        public String getText() {
            return this.questionText;
        }
    }
    
    // Nested class for Option DTOs
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Data transfer object for options in a question")
    public static class OptionDTO {
        @Schema(description = "Unique identifier for the option", example = "1")
        private Long id;
        
        @Schema(description = "Key/label for the option (e.g., A, B, C, D)", example = "A")
        private String optionKey;
        
        @Schema(description = "Text content of the option", example = "It prevents blood clotting")
        private String optionText;
        
        @Schema(description = "Flag indicating if this option is correct", example = "true")
        private Boolean isCorrect;
        
        // Additional setter for frontend compatibility with 'label' field
        @Schema(hidden = true) // Hide from OpenAPI documentation as it's just for compatibility
        public void setLabel(String label) {
            this.optionKey = label;
        }
        
        // Getter for frontend compatibility
        @Schema(description = "Alias for optionKey (for frontend compatibility)", example = "A")
        public String getLabel() {
            return this.optionKey;
        }
        
        // Additional setter for frontend compatibility with 'text' field
        @Schema(hidden = true) // Hide from OpenAPI documentation as it's just for compatibility
        public void setText(String text) {
            this.optionText = text;
        }
        
        // Getter for frontend compatibility
        @Schema(description = "Alias for optionText (for frontend compatibility)", example = "It prevents blood clotting")
        public String getText() {
            return this.optionText;
        }
    }
}