package com.pharmacyhub.security.service;

import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.exception.RBACException;
import com.pharmacyhub.security.infrastructure.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleHierarchyService {
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    @Transactional
    @CacheEvict(value = {"roleHierarchy", "userPermissions"}, allEntries = true)
    public void addChildRole(Long parentRoleId, Long childRoleId) {
        Role parentRole = roleRepository.findById(parentRoleId)
            .orElseThrow(() -> RBACException.entityNotFound("Parent role"));
        Role childRole = roleRepository.findById(childRoleId)
            .orElseThrow(() -> RBACException.entityNotFound("Child role"));

        // Check for circular dependency
        if (isCircularDependency(childRole, parentRoleId, new HashSet<>())) {
            throw RBACException.invalidRoleHierarchy();
        }

        // Check precedence
        if (childRole.getPrecedence() <= parentRole.getPrecedence()) {
            throw RBACException.invalidOperation("Child role must have lower precedence than parent role");
        }

        parentRole.getChildRoles().add(childRole);
        roleRepository.save(parentRole);

        auditService.logSecurityEvent(
            "ADD_CHILD_ROLE",
            String.format("Added role %s as child of %s", childRole.getName(), parentRole.getName()),
            "SUCCESS"
        );
    }

    @Transactional
    @CacheEvict(value = {"roleHierarchy", "userPermissions"}, allEntries = true)
    public void removeChildRole(Long parentRoleId, Long childRoleId) {
        Role parentRole = roleRepository.findById(parentRoleId)
            .orElseThrow(() -> RBACException.entityNotFound("Parent role"));
        Role childRole = roleRepository.findById(childRoleId)
            .orElseThrow(() -> RBACException.entityNotFound("Child role"));

        if (parentRole.getChildRoles().remove(childRole)) {
            roleRepository.save(parentRole);
            
            auditService.logSecurityEvent(
                "REMOVE_CHILD_ROLE",
                String.format("Removed role %s as child of %s", childRole.getName(), parentRole.getName()),
                "SUCCESS"
            );
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "roleHierarchy", key = "#roleId")
    public Set<Role> getAllChildRoles(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> RBACException.entityNotFound("Role"));

        Set<Role> allChildRoles = new HashSet<>();
        collectChildRoles(role, allChildRoles);
        return allChildRoles;
    }

    @Transactional(readOnly = true)
    public List<Role> getRolesByPrecedence() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "precedence"));
    }

    private boolean isCircularDependency(Role role, Long targetParentId, Set<Long> visited) {
        if (role.getId().equals(targetParentId)) {
            return true;
        }

        if (!visited.add(role.getId())) {
            return false;
        }

        return role.getChildRoles().stream()
            .anyMatch(childRole -> isCircularDependency(childRole, targetParentId, new HashSet<>(visited)));
    }

    private void collectChildRoles(Role role, Set<Role> allChildRoles) {
        role.getChildRoles().forEach(childRole -> {
            allChildRoles.add(childRole);
            collectChildRoles(childRole, allChildRoles);
        });
    }
}