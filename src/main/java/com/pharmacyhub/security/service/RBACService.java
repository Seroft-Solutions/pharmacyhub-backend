package com.pharmacyhub.security.service;

import com.pharmacyhub.engine.PHEngine;
import com.pharmacyhub.engine.PHMapper;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.AuditLog;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.dto.GroupDTO;
import com.pharmacyhub.security.dto.PermissionDTO;
import com.pharmacyhub.security.dto.RoleDTO;
import com.pharmacyhub.security.infrastructure.AuditLogRepository;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    public RBACService(
            UserRepository userRepository,
            RolesRepository rolesRepository,
            PermissionRepository permissionRepository,
            GroupRepository groupRepository,
            AuditLogRepository auditLogRepository,
            PHMapper phMapper)
    {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.permissionRepository = permissionRepository;
        this.groupRepository = groupRepository;
        this.auditLogRepository = auditLogRepository;
        this.phMapper = phMapper;
    }

    @Cacheable(value = "userPermissions", key = "#userId")
    public Set<Permission> getUserEffectivePermissions(Long userId)
    {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new RuntimeException("User not found"));

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

        return effectivePermissions;
    }

    private void addChildRolePermissions(Role role, Set<Permission> permissions)
    {
        role.getChildRoles().forEach(childRole -> {
            permissions.addAll(childRole.getPermissions());
            addChildRolePermissions(childRole, permissions);
        });
    }

    private void handlePermissionOverrides(User user, Set<Permission> permissions)
    {
        user.getPermissionOverrides().forEach(override -> {
            if (override.startsWith("-"))
            {
                // Remove permission
                permissions.removeIf(p -> p.getName().equals(override.substring(1)));
            }
            else
            {
                // Add permission
                permissionRepository.findByName(override)
                                    .ifPresent(permissions::add);
            }
        });
    }

    @PreAuthorize("hasPermission('ROLE', 'MANAGE')")
    public Role createRole(RoleDTO roleDTO)
    {
        validateRoleHierarchy(roleDTO);
        Role role = phMapper.getRole(roleDTO);
        role = rolesRepository.save(role);
        auditLogRepository.save(createAuditLog("CREATE_ROLE", role.getId()));
        return role;
    }

    @PreAuthorize("hasPermission('PERMISSION', 'MANAGE')")
    public Permission createPermission(PermissionDTO permissionDTO)
    {
        Permission permission = phMapper.getPermission(permissionDTO);
        permission = permissionRepository.save(permission);
        auditLogRepository.save(createAuditLog("CREATE_PERMISSION", permission.getId()));
        return permission;
    }

    @PreAuthorize("hasPermission('GROUP', 'MANAGE')")
    public Group createGroup(GroupDTO groupDTO)
    {
        Group group = phMapper.getGroup(groupDTO);
        group = groupRepository.save(group);
        auditLogRepository.save(createAuditLog("CREATE_GROUP", group.getId()));
        return group;
    }

    @PreAuthorize("hasPermission('ROLE', 'MANAGE')")
    public void assignRoleToUser(Long userId, Long roleId)
    {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = rolesRepository.findById(roleId)
                                   .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);
        userRepository.save(user);
        auditLogRepository.save(createAuditLog("ASSIGN_ROLE", userId, roleId));
    }

    @PreAuthorize("hasPermission('GROUP', 'MANAGE')")
    public void assignGroupToUser(Long userId, Long groupId)
    {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                                     .orElseThrow(() -> new RuntimeException("Group not found"));

        user.getGroups().add(group);
        userRepository.save(user);
        auditLogRepository.save(createAuditLog("ASSIGN_GROUP", userId, groupId));
    }

    @PreAuthorize("hasPermission('PERMISSION', 'MANAGE')")
    public void addPermissionOverride(Long userId, String permission, boolean grant)
    {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new RuntimeException("User not found"));

        String override = grant ? permission : "-" + permission;
        user.getPermissionOverrides().add(override);
        userRepository.save(user);
        auditLogRepository.save(createAuditLog("PERMISSION_OVERRIDE", userId));
    }

    private void validateRoleHierarchy(RoleDTO roleDTO)
    {
        // Validate no circular dependencies
        if (roleDTO.getChildRoleIds() != null)
        {
            Set<Long> visited = new HashSet<>();
            roleDTO.getChildRoleIds().forEach(childId ->
                                                      checkCircularDependency(childId, visited));
        }
    }

    private void checkCircularDependency(Long roleId, Set<Long> visited)
    {
        if (!visited.add(roleId))
        {
            throw new RuntimeException("Circular dependency detected in role hierarchy");
        }

        Role role = rolesRepository.findById(roleId)
                                   .orElseThrow(() -> new RuntimeException("Role not found"));

        role.getChildRoles().forEach(childRole ->
                                             checkCircularDependency(childRole.getId(), new HashSet<>(visited)));
    }

    private AuditLog createAuditLog(String action, Long... targetIds)
    {
        return AuditLog.builder()
                       .action(action)
                       .timestamp(LocalDateTime.now())
                       .build();
    }
}