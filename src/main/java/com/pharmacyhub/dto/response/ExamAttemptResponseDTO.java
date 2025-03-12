package com.pharmacyhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacyhub.domain.entity.ExamAttempt;
import com.pharmacyhub.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for exam attempt data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamAttemptResponseDTO implements BaseDTO {
    
    private Long id;
    private Long examId;
    private String examTitle;
    private String userId;
    private String startTime;
    private String endTime;
    private String status;
    private Integer questionsAnswered;
    private Integer questionsFlagged;
    private Integer timeSpent;
}
