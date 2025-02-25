package com.pharmacyhub.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ExamDTO {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be positive or zero")
    private Integer duration;
    
    @NotNull(message = "Total marks is required")
    @PositiveOrZero(message = "Total marks must be positive or zero")
    private Integer totalMarks;
    
    @NotNull(message = "Passing marks is required")
    @PositiveOrZero(message = "Passing marks must be positive or zero")
    private Integer passingMarks;
}
