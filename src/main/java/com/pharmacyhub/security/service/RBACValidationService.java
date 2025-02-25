package com.pharmacyhub.security.service;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.dto.GroupDTO;
import com.pharmacyhub.security.dto.PermissionDTO;
import com.pharmacyhub.security.dto.RoleDTO;
import com.pharmacyhub.security.exception.RBACException;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service for validating RBAC operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RBACValidationService {
    private final RolesRepository rolesRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;

    /**
     * Validate role creation
     */
    public void validateRoleCreation(RoleDTO roleDTO) {
        // Check for missing name
        if (roleDTO.getName() == null) {
            throw RBACException.invalidData("Role name cannot be null");
        }
        
        try {
            // Convert string to RoleEnum
            RoleEnum roleEnum = validateRoleEnum(roleDTO.getName());
            
            // Check if role with same name already exists
            if (rolesRepository.findByName(roleEnum).isPresent()) {
                throw RBACException.alreadyExists("Role with name " + roleDTO.getName() + " already exists");
            }
        } catch (IllegalArgumentException e) {
            throw RBACException.invalidData("Invalid role name: " + roleDTO.getName());
        }
        
        // Validate permissions if specified
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            for (Long permissionId : roleDTO.getPermissionIds()) {
                if (!permissionRepository.existsById(permissionId)) {
                    throw RBACException.invalidData("Permission with ID " + permissionId + " does not exist");
                }
            }
        }
    }
    
    /**
     * Validate permission creation
     */
    public void validatePermissionCreation(PermissionDTO permissionDTO) {
        // Check for missing name
        if (permissionDTO.getName() == null || permissionDTO.getName().isEmpty()) {
            throw RBACException.invalidData("Permission name cannot be null or empty");
        }
        
        // Check if permission with same name already exists
        if (permissionRepository.findByName(permissionDTO.getName()).isPresent()) {
            throw RBACException.alreadyExists("Permission with name " + permissionDTO.getName() + " already exists");
        }
        
        // Check if resource type is valid
        if (permissionDTO.getResourceType() == null) {
            log.warn("Resource type not specified for permission {}, defaulting to USER", permissionDTO.getName());
            permissionDTO.setResourceType(ResourceType.USER);
        }
        
        // Check if operation type is valid
        if (permissionDTO.getOperationType() == null) {
            throw RBACException.invalidData("Operation type cannot be null for permission " + permissionDTO.getName());
        }
    }
    
    /**
     * Validate group creation
     */
    public void validateGroupCreation(GroupDTO groupDTO) {
        // Check for missing name
        if (groupDTO.getName() == null || groupDTO.getName().isEmpty()) {
            throw RBACException.invalidData("Group name cannot be null or empty");
        }
        
        // Check if group with same name already exists
        if (groupRepository.findByName(groupDTO.getName()).isPresent()) {
            throw RBACException.alreadyExists("Group with name " + groupDTO.getName() + " already exists");
        }
        
        // Validate roles if specified
        if (groupDTO.getRoleIds() != null && !groupDTO.getRoleIds().isEmpty()) {
            for (Long roleId : groupDTO.getRoleIds()) {
                if (!rolesRepository.existsById(roleId)) {
                    throw RBACException.invalidData("Role with ID " + roleId + " does not exist");
                }
            }
        }
    }
    
    /**
     * Validate user role assignment
     */
    public void validateRoleAssignment(Long userId, Long roleId) {
        if (userId == null) {
            throw RBACException.invalidData("User ID cannot be null");
        }
        
        if (roleId == null) {
            throw RBACException.invalidData("Role ID cannot be null");
        }
        
        if (!rolesRepository.existsById(roleId)) {
            throw RBACException.invalidData("Role with ID " + roleId + " does not exist");
        }
    }
    
    /**
     * Validate user group assignment
     */
    public void validateGroupAssignment(Long userId, Long groupId) {
        if (userId == null) {
            throw RBACException.invalidData("User ID cannot be null");
        }
        
        if (groupId == null) {
            throw RBACException.invalidData("Group ID cannot be null");
        }
        
        if (!groupRepository.existsById(groupId)) {
            throw RBACException.invalidData("Group with ID " + groupId + " does not exist");
        }
    }
    
    /**
     * Validate permission override
     */
    public void validatePermissionOverride(Long userId, String permission, boolean grant) {
        if (userId == null) {
            throw RBACException.invalidData("User ID cannot be null");
        }
        
        if (permission == null || permission.isEmpty()) {
            throw RBACException.invalidData("Permission name cannot be null or empty");
        }
        
        // If granting, verify the permission exists
        if (grant && permissionRepository.findByName(permission).isEmpty()) {
            throw RBACException.invalidData("Permission with name " + permission + " does not exist");
        }
    }
    
    /**
     * Validate role hierarchy management
     */
    public void validateRoleHierarchy(Long parentRoleId, Long childRoleId) {
        if (parentRoleId == null || childRoleId == null) {
            throw RBACException.invalidData("Parent and child role IDs cannot be null");
        }
        
        if (parentRoleId.equals(childRoleId)) {
            throw RBACException.invalidData("A role cannot be its own child");
        }
        
        Role parentRole = rolesRepository.findById(parentRoleId)
            .orElseThrow(() -> RBACException.entityNotFound("Parent role"));
            
        Role childRole = rolesRepository.findById(childRoleId)
            .orElseThrow(() -> RBACException.entityNotFound("Child role"));
            
        // Ensure parent role has higher precedence than child role
        if (parentRole.getPrecedence() >= childRole.getPrecedence()) {
            throw RBACException.invalidData(
                "Parent role must have higher precedence (lower value) than child role. " +
                "Parent precedence: " + parentRole.getPrecedence() + 
                ", Child precedence: " + childRole.getPrecedence());
        }
        
        // Check for circular dependencies
        if (hasCircularDependency(childRole, parentRoleId)) {
            throw RBACException.invalidData("Adding this hierarchy would create a circular dependency");
        }
    }
    
    /**
     * Check if adding a parent role would create a circular dependency
     */
    private boolean hasCircularDependency(Role role, Long potentialParentId) {
        // Load the role with its child roles
        Role loadedRole = rolesRepository.findByIdWithChildRoles(role.getId());
        
        // Check if any of the child roles is the potential parent
        for (Role childRole : loadedRole.getChildRoles()) {
            if (childRole.getId().equals(potentialParentId)) {
                return true;
            }
            
            // Recursively check child roles
            if (hasCircularDependency(childRole, potentialParentId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate enum values from string
     */
    public RoleEnum validateRoleEnum(String roleName) {
        try {
            return RoleEnum.fromString(roleName);
        } catch (IllegalArgumentException e) {
            throw RBACException.invalidData("Invalid role name: " + roleName);
        }
    }
    
    /**
     * Validate resource type from string
     */
    public ResourceType validateResourceType(String resourceType) {
        try {
            return ResourceType.valueOf(resourceType);
        } catch (IllegalArgumentException e) {
            throw RBACException.invalidData("Invalid resource type: " + resourceType);
        }
    }
}