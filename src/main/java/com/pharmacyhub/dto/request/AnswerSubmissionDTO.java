package com.pharmacyhub.dto.request;

import com.pharmacyhub.dto.BaseDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for submitting an answer to a question
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSubmissionDTO implements BaseDTO {
    
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    private String selectedOptionId;
    
    @NotNull(message = "Time spent is required")
    @PositiveOrZero(message = "Time spent must be zero or positive")
    private Integer timeSpent; // in seconds
}
