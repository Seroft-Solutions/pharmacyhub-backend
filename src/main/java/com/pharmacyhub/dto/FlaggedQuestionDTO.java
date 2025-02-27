package com.pharmacyhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlaggedQuestionDTO {
    private Long attemptId;
    private Long questionId;
}
