package com.pharmacyhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamStatsDTO {
    
    private Integer totalPapers;
    private Integer avgDuration;
    private Integer completionRate;
    private Integer activeUsers;
}
