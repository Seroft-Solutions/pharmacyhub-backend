package com.pharmacyhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttemptDTO {
    
    private Long id;
    private Long examId;
    private String userId;
    private String startTime;
    private List<UserAnswerDTO> answers;
    private String status;
}
