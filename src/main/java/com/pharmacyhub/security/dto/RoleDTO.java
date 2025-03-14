package com.pharmacyhub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Role entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private boolean system;
    private int precedence;
    @Builder.Default
    private List<Long> permissionIds = new ArrayList<>();
    @Builder.Default
    private List<Long> childRoleIds = new ArrayList<>();
}
