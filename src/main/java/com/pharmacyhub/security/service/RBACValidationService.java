package com.pharmacyhub.security.service;

import java.util.HashSet;
import java.util.Set;

import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.application.dto.GroupDTO;
import com.pharmacyhub.security.application.dto.PermissionDTO;
import com.pharmacyhub.security.application.dto.RoleDTO;
import com.pharmacyhub.security.domain.exception.RBACException;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RBACValidationService {

 	private final RolesRepository rolesRepository;
 	private final PermissionRepository permissionRepository;
 	private final GroupRepository groupRepository;

 	public void validateRoleCreation(com.pharmacyhub.security.application.dto.RoleDTO roleDTO) {
 		// Check for name uniqueness
 		try {
 			String roleName = roleDTO.getName();
 			if (rolesRepository.findByName(roleName).isPresent()) {
 				throw RBACException.invalidOperation("Role name already exists");
 			}
 		} catch (IllegalArgumentException e) {
 			throw RBACException.invalidOperation("Invalid role name: " + roleDTO.getName());
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

 	public void validatePermissionCreation(com.pharmacyhub.security.application.dto.PermissionDTO permissionDTO) {
 		// Check for name uniqueness
 		if (permissionRepository.findByName(permissionDTO.getName()).isPresent()) {
 			throw RBACException.invalidOperation("Permission name already exists");
 		}

 		// Validate resource type and operation type combination
 		validateResourceOperationCombination(permissionDTO);
 	}

 	public void validateGroupCreation(com.pharmacyhub.security.application.dto.GroupDTO groupDTO) {
 		// Check for name uniqueness
 		if (groupRepository.findByName(groupDTO.getName()).isPresent()) {
 			throw RBACException.invalidOperation("Group name already exists");
 		}

 		// Validate roles exist
 		if (groupDTO.getRoleIds() != null) {
 			groupDTO.getRoleIds().forEach(roleId -> {
 				if (!rolesRepository.existsById(roleId)) {
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

 			Role role = rolesRepository.findByIdWithChildRoles(roleId);
 			if (role == null) {
 				throw RBACException.entityNotFound("Role");
 			}

 

 			if (!role.getChildRoles().isEmpty()) {
 				validateRoleHierarchy(role.getChildRoles()
 						.stream()
 						.map(Role::getId)
 						.collect(java.util.stream.Collectors.toSet()), new HashSet<>(visitedRoles));
 			}
 		}
 	}

 	private void validateResourceOperationCombination(com.pharmacyhub.security.application.dto.PermissionDTO permissionDTO) {
 		// Add specific validation rules for resource and operation combinations
 		switch (permissionDTO.getResourceType()) {
 		case INVENTORY:
 			validateInventoryOperations(permissionDTO.getOperationType());
 			break;
 		case PRESCRIPTION:
 			validatePrescriptionOperations(permissionDTO.getOperationType());
 		}
 	}

 	private void validateInventoryOperations(OperationType operationType) {
 		switch (operationType) {
 		case CREATE:
 		case READ:
 		case UPDATE:
 		case DELETE:
 		case MANAGE:
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
 			break;
 		default:
 			throw RBACException.invalidOperation("Invalid operation type for Prescription resource");
 		}
 	}
}