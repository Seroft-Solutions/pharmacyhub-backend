package com.pharmacyhub.security.service;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.exception.RBACException;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, String roleName) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> RBACException.entityNotFound("Role"));
        return hasRole(userId, role);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyRole(Long userId, String... roleNames) {
        return Arrays.stream(roleNames)
            .anyMatch(roleName -> hasRole(userId, roleName));
    }

    @Transactional(readOnly = true)
    public boolean isInGroup(Long userId, String groupName) {
        Group group = groupRepository.findByName(groupName)
            .orElseThrow(() -> RBACException.entityNotFound("Group"));
        return hasGroup(userId, group);
    }

    @Transactional(readOnly = true)
    public boolean canAssignRole(User assignerUser, Role roleToAssign) {
        // Get all roles of the assigner
        Set<Role> assignerRoles = assignerUser.getRoles();

        // Check if any of the assigner's roles have higher or equal precedence
        return assignerRoles.stream()
            .anyMatch(assignerRole -> assignerRole.getPrecedence() <= roleToAssign.getPrecedence());
    }

    @Transactional(readOnly = true)
    public boolean canRemoveRole(User removerUser, Role roleToRemove) {
        // Similar to assigning, but also prevent users from removing their own roles
        if (removerUser.getRoles().contains(roleToRemove)) {
            return false;
        }

        // Get all roles of the remover
        Set<Role> removerRoles = removerUser.getRoles();

        // Check if any of the remover's roles have higher or equal precedence
        return removerRoles.stream()
            .anyMatch(removerRole -> removerRole.getPrecedence() <= roleToRemove.getPrecedence());
    }

    private boolean hasRole(Long userId, Role role) {
        // This would be implemented by checking the user's roles in the database
        return false; // Placeholder implementation
    }

    private boolean hasGroup(Long userId, Group group) {
        // This would be implemented by checking the user's groups in the database
        return false; // Placeholder implementation
    }
}
