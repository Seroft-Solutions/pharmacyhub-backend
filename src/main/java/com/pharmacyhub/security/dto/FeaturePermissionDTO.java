package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for feature permission mappings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturePermissionDTO {
    private String name;
    private String description;
}
