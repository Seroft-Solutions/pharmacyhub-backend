package com.pharmacyhub.security.application.dto;

import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {
    private Long id;
    private String name;
    private String description;
    private ResourceType resourceType;
    private OperationType operationType;
    private boolean requiresApproval;
    private String conditions;
}