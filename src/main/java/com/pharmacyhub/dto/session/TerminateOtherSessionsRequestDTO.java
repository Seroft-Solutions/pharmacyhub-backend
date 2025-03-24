package com.pharmacyhub.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for terminating other sessions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminateOtherSessionsRequestDTO {
    
    @NotNull(message = "Current session ID is required")
    @JsonProperty("currentSessionId")
    private UUID currentSessionId;
}
