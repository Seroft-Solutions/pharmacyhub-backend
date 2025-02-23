package com.pharmacyhub.security.service;

import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.dto.GroupDTO;
import com.pharmacyhub.security.dto.PermissionDTO;
import com.pharmacyhub.security.dto.RoleDTO;
import com.pharmacyhub.security.exception.RBACException;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RBACValidationService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;

    public void validateRoleCreation(RoleDTO roleDTO) {
        // Check for name uniqueness
        if (roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw RBACException.invalidOperation("Role name already exists");
        }

        // Validate permissions exist
        if (roleDTO.getPermissionIds() != null) {
            roleDTO.getPermissionIds().forEach(permissionId -> {
                if (!permissionRepository.existsById(permissionId)) {
                    throw RBACException.entityNotFound("Permission");
                }
            });
        }

        // Validate child roles
        if (roleDTO.getChildRoleIds() != null) {
            validateRoleHierarchy(roleDTO.getChildRoleIds(), new HashSet<>());
        }
    }

    public void validatePermissionCreation(PermissionDTO permissionDTO) {
        // Check for name uniqueness
        if (permissionRepository.findByName(permissionDTO.getName()).isPresent()) {
            throw RBACException.invalidOperation("Permission name already exists");
        }

        // Validate resource type and operation type combination
        validateResourceOperationCombination(permissionDTO);
    }

    public void validateGroupCreation(GroupDTO groupDTO) {
        // Check for name uniqueness
        if (groupRepository.findByName(groupDTO.getName()).isPresent()) {
            throw RBACException.invalidOperation("Group name already exists");
        }

        // Validate roles exist
        if (groupDTO.getRoleIds() != null) {
            groupDTO.getRoleIds().forEach(roleId -> {
                if (!roleRepository.existsById(roleId)) {
                    throw RBACException.entityNotFound("Role");
                }
            });
        }
    }

    private void validateRoleHierarchy(Set<Long> roleIds, Set<Long> visitedRoles) {
        for (Long roleId : roleIds) {
            if (!visitedRoles.add(roleId)) {
                throw RBACException.invalidRoleHierarchy();
            }

            Role role = roleRepository.findByIdWithChildRoles(roleId)
                .orElseThrow(() -> RBACException.entityNotFound("Role"));

            if (!role.getChildRoles().isEmpty()) {
                validateRoleHierarchy(
                    role.getChildRoles().stream().map(Role::getId).collect(java.util.stream.Collectors.toSet()),
                    new HashSet<>(visitedRoles)
                );
            }
        }
    }

    private void validateResourceOperationCombination(PermissionDTO permissionDTO) {
        // Add specific validation rules for resource and operation combinations
        switch (permissionDTO.getResourceType()) {
            case INVENTORY:
                validateInventoryOperations(permissionDTO.getOperationType());
                break;
            case PRESCRIPTION:
                validatePrescriptionOperations(permissionDTO.getOperationType());
                break;
            // Add more resource-specific validations
        }
    }

    private void validateInventoryOperations(OperationType operationType) {
        switch (operationType) {
            case CREATE:
            case READ:
            case UPDATE:
            case DELETE:
            case MANAGE:
            case VIEW_ALL:
                break;
            default:
                throw RBACException.invalidOperation("Invalid operation type for Inventory resource");
        }
    }

    private void validatePrescriptionOperations(OperationType operationType) {
        switch (operationType) {
            case CREATE:
            case READ:
            case UPDATE:
            case APPROVE:
            case REJECT:
            case VIEW_ALL:
                break;
            default:
                throw RBACException.invalidOperation("Invalid operation type for Prescription resource");
        }
    }
}