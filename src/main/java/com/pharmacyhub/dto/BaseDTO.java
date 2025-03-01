package com.pharmacyhub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Base interface for all DTOs
 * Provides common behavior and serves as a marker interface
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface BaseDTO {
    // Marker interface for all DTOs
    // Common methods can be added here
}
