package com.pharmacyhub.security.service;

import com.pharmacyhub.engine.PHEngine;
import com.pharmacyhub.engine.PHMapper;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.AuditLog;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.application.dto.GroupDTO;
import com.pharmacyhub.security.application.dto.PermissionDTO;
import com.pharmacyhub.security.application.dto.RoleDTO;
import com.pharmacyhub.security.exception.RBACException;
import com.pharmacyhub.security.infrastructure.AuditLogRepository;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class RBACService extends PHEngine
{
    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;
    private final AuditLogRepository auditLogRepository;
    private final PHMapper phMapper;
    private final AuditService auditService;
    private final RBACValidationService validationService;

    public RBACService(
            UserRepository userRepository,
            RolesRepository rolesRepository,
            PermissionRepository permissionRepository,
            GroupRepository groupRepository,
            AuditLogRepository auditLogRepository,
            PHMapper phMapper,
            AuditService auditService,
            RBACValidationService validationService)
    {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.permissionRepository = permissionRepository;
        this.groupRepository = groupRepository;
        this.auditLogRepository = auditLogRepository;
        this.phMapper = phMapper;
        this.auditService = auditService;
        this.validationService = validationService;
    }

    @Cacheable(value = "userPermissions", key = "#userId")
    public Set<Permission> getUserEffectivePermissions(Long userId)
    {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> RBACException.entityNotFound("User"));

        Set<Permission> effectivePermissions = new HashSet<>();

        // Add permissions from roles
        user.getRoles().forEach(role -> {
            effectivePermissions.addAll(role.getPermissions());
            addChildRolePermissions(role, effectivePermissions);
        });

        // Add permissions from groups
        user.getGroups().forEach(group -> {
            group.getRoles().forEach(role -> {
                effectivePermissions.addAll(role.getPermissions());
                addChildRolePermissions(role, effectivePermissions);
            });
        });

        // Handle permission overrides
        handlePermissionOverrides(user, effectivePermissions);
        
        log.debug("Computed {} effective permissions for user ID {}", effectivePermissions.size(), userId);
        return effectivePermissions;
    }

    private void addChildRolePermissions(Role role, Set<Permission> permissions)
    {
        Set<Role> childRoles = role.getChildRoles();
        if (childRoles != null) {
            childRoles.forEach(childRole -> {
                permissions.addAll(childRole.getPermissions());
                addChildRolePermissions(childRole, permissions);
            });
        }
    }
    
    /**
     * Get all roles assigned to a user, including roles from groups.
     */
    @Cacheable(value = "userRoles", key = "#userId")
    public Set<Role> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RBACException.entityNotFound("User"));

        Set<Role> allRoles = new HashSet<>(user.getRoles());
        
        // Add roles from groups
        user.getGroups().forEach(group -> 
            allRoles.addAll(group.getRoles())
        );
        
        return allRoles;
    }

    private void handlePermissionOverrides(User user, Set<Permission> permissions)
    {
        user.getPermissionOverrides().forEach(override -> {
            if (override.startsWith("-"))
            {
                // Remove permission
                permissions.removeIf(p -> p.getName().equals(override.substring(1)));
                log.debug("Removed permission {} due to override for user {}", override.substring(1), user.getId());
            }
            else
            {
                // Add permission
                permissionRepository.findByName(override)
                                    .ifPresent(permission -> {
                                        permissions.add(permission);
                                        log.debug("Added permission {} due to override for user {}", permission.getName(), user.getId());
                                    });
            }
        });
    }

    @PreAuthorize("hasPermission('ROLE', 'MANAGE')")
    @CacheEvict(value = {"roleHierarchy", "userPermissions", "userRoles"}, allEntries = true)
    public Role createRole(RoleDTO roleDTO)
    {
        validationService.validateRoleCreation(roleDTO);
        Role role = phMapper.getRole(roleDTO);
        role = rolesRepository.save(role);
        
        auditService.logSecurityEvent(
            "CREATE_ROLE",
            String.format("Created role '%s'", role.getName()),
            "SUCCESS"
        );
        
        return role;
    }
    
    /**
     * Update an existing role.
     */
    @PreAuthorize("hasPermission('ROLE', 'MANAGE')")
    @CacheEvict(value = {"roleHierarchy", "userPermissions", "userRoles"}, allEntries = true)
    public Role updateRole(Long roleId, RoleDTO roleDTO) {
        Role existingRole = rolesRepository.findById(roleId)
                .orElseThrow(() -> RBACException.entityNotFound("Role"));
        
        // Check if system role is being modified
        if (existingRole.isSystem() && !roleDTO.isSystem()) {
            throw RBACException.invalidOperation("Cannot change system role status");
        }
        
        // Update fields
        if (roleDTO.getDescription() != null) {
            existingRole.setDescription(roleDTO.getDescription());
        }
        
        if (roleDTO.getPrecedence() > 0) {
            existingRole.setPrecedence(roleDTO.getPrecedence());
        }
        
        // Update permissions
        if (roleDTO.getPermissionIds() != null) {
            Set<Permission> permissions = roleDTO.getPermissionIds().stream()
                    .map(id -> permissionRepository.findById(id)
                            .orElseThrow(() -> RBACException.entityNotFound("Permission")))
                    .collect(Collectors.toSet());
            existingRole.setPermissions(permissions);
        }
        
        Role updatedRole = rolesRepository.save(existingRole);
        
        auditService.logSecurityEvent(
            "UPDATE_ROLE",
            String.format("Updated role '%s'", updatedRole.getName()),
            "SUCCESS"
        );
        
        return updatedRole;
    }

    @PreAuthorize("hasPermission('PERMISSION', 'MANAGE')")
    @CacheEvict(value = {"userPermissions"}, allEntries = true)
    public Permission createPermission(PermissionDTO permissionDTO)
    {
        validationService.validatePermissionCreation(permissionDTO);
        
        Permission permission = phMapper.getPermission(permissionDTO);
        permission = permissionRepository.save(permission);
        
        auditService.logSecurityEvent(
            "CREATE_PERMISSION",
            String.format("Created permission '%s'", permission.getName()),
            "SUCCESS"
        );
        
        return permission;
    }

    @PreAuthorize("hasPermission('GROUP', 'MANAGE')")
    @CacheEvict(value = {"groupRoles", "userPermissions", "userRoles"}, allEntries = true)
    public Group createGroup(GroupDTO groupDTO)
    {
        validationService.validateGroupCreation(groupDTO);
        
        Group group = phMapper.getGroup(groupDTO);
        group = groupRepository.save(group);
        
        auditService.logSecurityEvent(
            "CREATE_GROUP",
            String.format("Created group '%s'", group.getName()),
            "SUCCESS"
        );
        
        return group;
    }

    @PreAuthorize("hasPermission('ROLE', 'ASSIGN')")
    @CacheEvict(value = {"userPermissions", "userRoles"}, key = "#userId")
    public void assignRoleToUser(Long userId, Long roleId)
    {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> RBACException.entityNotFound("User"));
        Role role = rolesRepository.findById(roleId)
                                   .orElseThrow(() -> RBACException.entityNotFound("Role"));

        // Check if user already has the role
        if (user.getRoles().stream().anyMatch(r -> r.getId().equals(roleId))) {
            log.debug("User {} already has role {}", userId, roleId);
            return;
        }

        user.getRoles().add(role);
        userRepository.save(user);
        
        auditService.logSecurityEvent(
            "ASSIGN_ROLE",
            String.format("Assigned role '%s' to user '%s'", role.getName(), user.getUsername()),
            "SUCCESS"
        );
    }
    
    /**
     * Remove a role from a user.
     */
    @PreAuthorize("hasPermission('ROLE', 'ASSIGN')")
    @CacheEvict(value = {"userPermissions", "userRoles"}, key = "#userId")
    public void removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RBACException.entityNotFound("User"));
                
        Role role = rolesRepository.findById(roleId)
                .orElseThrow(() -> RBACException.entityNotFound("Role"));

        if (user.getRoles().removeIf(r -> r.getId().equals(roleId))) {
            userRepository.save(user);
            
            auditService.logSecurityEvent(
                "REMOVE_ROLE",
                String.format("Removed role '%s' from user '%s'", role.getName(), user.getUsername()),
                "SUCCESS"
            );
        }
    }

    @PreAuthorize("hasPermission('GROUP', 'ASSIGN')")
    @CacheEvict(value = {"userPermissions", "userRoles"}, key = "#userId")
    public void assignGroupToUser(Long userId, Long groupId)
    {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> RBACException.entityNotFound("User"));
        Group group = groupRepository.findById(groupId)
                                     .orElseThrow(() -> RBACException.entityNotFound("Group"));

        // Check if user already has the group
        if (user.getGroups().stream().anyMatch(g -> g.getId().equals(groupId))) {
            log.debug("User {} already has group {}", userId, groupId);
            return;
        }

        user.getGroups().add(group);
        userRepository.save(user);
        
        auditService.logSecurityEvent(
            "ASSIGN_GROUP",
            String.format("Assigned group '%s' to user '%s'", group.getName(), user.getUsername()),
            "SUCCESS"
        );
    }
    
    /**
     * Remove a group from a user.
     */
    @PreAuthorize("hasPermission('GROUP', 'ASSIGN')")
    @CacheEvict(value = {"userPermissions", "userRoles"}, key = "#userId")
    public void removeGroupFromUser(Long userId, Long groupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RBACException.entityNotFound("User"));
                
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> RBACException.entityNotFound("Group"));

        if (user.getGroups().removeIf(g -> g.getId().equals(groupId))) {
            userRepository.save(user);
            
            auditService.logSecurityEvent(
                "REMOVE_GROUP",
                String.format("Removed group '%s' from user '%s'", group.getName(), user.getUsername()),
                "SUCCESS"
            );
        }
    }

    @PreAuthorize("hasPermission('PERMISSION', 'MANAGE')")
    @CacheEvict(value = {"userPermissions"}, key = "#userId")
    public void addPermissionOverride(Long userId, String permission, boolean grant)
    {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> RBACException.entityNotFound("User"));

        String override = grant ? permission : "-" + permission;
        
        // Check if the override already exists
        if (user.getPermissionOverrides().contains(override)) {
            log.debug("Permission override {} already exists for user {}", override, userId);
            return;
        }
        
        user.getPermissionOverrides().add(override);
        userRepository.save(user);
        
        auditService.logSecurityEvent(
            "PERMISSION_OVERRIDE",
            String.format("Added permission override '%s' for user '%s'", override, user.getUsername()),
            "SUCCESS"
        );
    }
    
    /**
     * Remove a permission override from a user.
     */
    @PreAuthorize("hasPermission('PERMISSION', 'MANAGE')")
    @CacheEvict(value = {"userPermissions"}, key = "#userId")
    public void removePermissionOverride(Long userId, String override) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RBACException.entityNotFound("User"));

        if (user.getPermissionOverrides().remove(override)) {
            userRepository.save(user);
            
            auditService.logSecurityEvent(
                "REMOVE_PERMISSION_OVERRIDE",
                String.format("Removed permission override '%s' for user '%s'", override, user.getUsername()),
                "SUCCESS"
            );
        }
    }

    /**
     * Check if a user has a specific permission.
     */
    @Cacheable(value = "userHasPermission", key = "#userId + '_' + #permissionName")
    public boolean userHasPermission(Long userId, String permissionName) {
        Set<Permission> permissions = getUserEffectivePermissions(userId);
        return permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    /**
     * Check if a user has a specific role.
     */
    @Cacheable(value = "userHasRole", key = "#userId + '_' + #roleName")
    public boolean userHasRole(Long userId, String roleName) {
        Set<Role> roles = getUserRoles(userId);
        return roles.stream()
                .anyMatch(r -> r.getName().equals(roleName));
    }

    /**
     * Get all users with a specific role.
     */
    @PreAuthorize("hasPermission('USER', 'READ')")
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRolesName(roleName);
    }

    /**
     * Get all users in a specific group.
     */
    @PreAuthorize("hasPermission('GROUP', 'READ')")
    public List<User> getUsersByGroup(String groupName) {
        return userRepository.findByGroupsName(groupName);
    }

    /**
     * Validate user access to a specific resource.
     */
    public boolean validateAccess(Long userId, String resourceType, String operation, Long resourceId) {
        Set<Permission> permissions = getUserEffectivePermissions(userId);
        
        // Check for exact permission match
        boolean hasPermission = permissions.stream()
                .anyMatch(p -> p.getResourceType().name().equals(resourceType) && 
                        p.getOperationType().name().equals(operation));
        
        if (hasPermission) {
            auditService.logSecurityEvent(
                "ACCESS_VALIDATION",
                String.format("User ID %d accessed %s:%s for resource ID %d", 
                    userId, resourceType, operation, resourceId),
                "GRANTED"
            );
            return true;
        } else {
            auditService.logSecurityEvent(
                "ACCESS_VALIDATION",
                String.format("User ID %d denied access to %s:%s for resource ID %d", 
                    userId, resourceType, operation, resourceId),
                "DENIED"
            );
            return false;
        }
    }
}
