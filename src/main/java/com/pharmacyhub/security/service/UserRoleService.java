package com.pharmacyhub.security.service;

import com.pharmacyhub.engine.PHEngine;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRoleService extends PHEngine {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final AuditService auditService;
    private final RBACService rbacService;

    @Transactional
    public void assignRolesToUser(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if current user has permission to assign these roles
        Set<Role> rolesToAssign = roleRepository.findByIdInWithPermissions(roleIds);
        validateRoleAssignment(rolesToAssign);
        
        user.setRoles(rolesToAssign);
        userRepository.save(user);
        
        auditService.logSecurityEvent(
            "ASSIGN_ROLES",
            "Assigned roles " + roleIds + " to user " + userId,
            "SUCCESS"
        );
    }

    @Transactional
    public void assignGroupsToUser(Long userId, Set<Long> groupIds) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if current user has permission to assign these groups
        Set<Group> groupsToAssign = groupRepository.findByIdInWithRoles(groupIds);
        validateGroupAssignment(groupsToAssign);
        
        user.setGroups(groupsToAssign);
        userRepository.save(user);
        
        auditService.logSecurityEvent(
            "ASSIGN_GROUPS",
            "Assigned groups " + groupIds + " to user " + userId,
            "SUCCESS"
        );
    }

    private void validateRoleAssignment(Set<Role> rolesToAssign) {
        User currentUser = getLoggedInUser();
        Set<Role> currentUserRoles = currentUser.getRoles();
        
        // Get the highest precedence (lowest number) among current user's roles
        int currentUserHighestPrecedence = currentUserRoles.stream()
            .mapToInt(Role::getPrecedence)
            .min()
            .orElse(Integer.MAX_VALUE);
            
        // Check if trying to assign any role with higher or equal precedence
        boolean hasInvalidAssignment = rolesToAssign.stream()
            .anyMatch(role -> role.getPrecedence() <= currentUserHighestPrecedence);
            
        if (hasInvalidAssignment) {
            throw new AccessDeniedException("Cannot assign roles with higher or equal precedence");
        }
    }

    private void validateGroupAssignment(Set<Group> groupsToAssign) {
        User currentUser = getLoggedInUser();
        Set<Role> currentUserRoles = currentUser.getRoles();
        
        int currentUserHighestPrecedence = currentUserRoles.stream()
            .mapToInt(Role::getPrecedence)
            .min()
            .orElse(Integer.MAX_VALUE);
            
        // Check if any group contains roles with higher or equal precedence
        boolean hasInvalidAssignment = groupsToAssign.stream()
            .flatMap(group -> group.getRoles().stream())
            .anyMatch(role -> role.getPrecedence() <= currentUserHighestPrecedence);
            
        if (hasInvalidAssignment) {
            throw new AccessDeniedException("Cannot assign groups containing roles with higher or equal precedence");
        }
    }
}