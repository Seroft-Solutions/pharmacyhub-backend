package com.pharmacyhub.security.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private Set<Long> permissionIds;
    private Set<Long> childRoleIds;
    private Integer precedence;
    private boolean system;
}