package com.pharmacyhub.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for filtering sessions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionFilterCriteriaDTO {
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("active")
    private Boolean active;
    
    @JsonProperty("suspicious")
    private Boolean suspicious;
    
    @JsonProperty("fromDate")
    private String fromDate;
    
    @JsonProperty("toDate")
    private String toDate;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("requiresOtp")
    private Boolean requiresOtp;
}
