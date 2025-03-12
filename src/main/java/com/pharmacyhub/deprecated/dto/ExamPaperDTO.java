package com.pharmacyhub.deprecated.dto;

// DEPRECATED: This DTO is for the deprecated ExamPaper entity
// Use ExamResponseDTO for all new development

import com.pharmacyhub.deprecated.entity.ExamPaper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaperDTO {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Difficulty is required")
    private String difficulty;
    
    @NotNull(message = "Question count is required")
    @PositiveOrZero(message = "Question count must be positive or zero")
    private Integer questionCount;
    
    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be positive or zero")
    private Integer durationMinutes;
    
    private Set<String> tags;
    
    @NotNull(message = "Premium status is required")
    private Boolean premium;
    
    private Integer attemptCount;
    
    private Double successRatePercent;
    
    private String lastUpdatedDate;
    
    @NotNull(message = "Type is required")
    private String type;
    
    private Long examId;
}
