package com.pharmacyhub.dto.request;

import com.pharmacyhub.domain.entity.Exam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class JsonExamUploadRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be positive or zero")
    private Integer duration; // in minutes
    
    @PositiveOrZero(message = "Passing marks must be positive or zero")
    private Integer passingMarks;
    
    private Exam.ExamStatus status = Exam.ExamStatus.DRAFT;
    
    private List<String> tags;
    
    // Paper type (MODEL, PAST, SUBJECT, PRACTICE)
    private String paperType;
    
    // Paper-specific metadata fields
    private Map<String, Object> metadata;
    
    // The JSON content as a string (will be parsed by the service)
    @NotBlank(message = "JSON content is required")
    private String jsonContent;
}