package com.pharmacyhub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerDTO {
    
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    private String selectedOptionId;
    
    @NotNull(message = "Time spent is required")
    @PositiveOrZero(message = "Time spent must be positive or zero")
    private Integer timeSpent; // in seconds
}
