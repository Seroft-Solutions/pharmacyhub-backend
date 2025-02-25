package com.pharmacyhub.controller;

import com.pharmacyhub.security.annotation.RequiresPermission;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.dto.GroupDTO;
import com.pharmacyhub.security.dto.PermissionDTO;
import com.pharmacyhub.security.dto.RoleDTO;
import com.pharmacyhub.security.service.RBACService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
public class RBACController {
    private final RBACService rbacService;

    @PostMapping("/roles")
    @RequiresPermission(resource = ResourceType.ROLE, operation = OperationType.CREATE)
    public ResponseEntity<?> createRole(@RequestBody RoleDTO roleDTO) {
        return ResponseEntity.ok(rbacService.createRole(roleDTO));
    }

    @PostMapping("/permissions")
    @RequiresPermission(resource = ResourceType.PERMISSION, operation = OperationType.CREATE)
    public ResponseEntity<PermissionDTO> createPermission(@RequestBody PermissionDTO permissionDTO) {
        return ResponseEntity.ok(rbacService.createPermission(permissionDTO));
    }

    @PostMapping("/groups")
    @RequiresPermission(resource = ResourceType.GROUP, operation = OperationType.CREATE)
    public ResponseEntity<?> createGroup(@RequestBody GroupDTO groupDTO) {
        return ResponseEntity.ok(rbacService.createGroup(groupDTO));
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    @RequiresPermission(resource = ResourceType.ROLE, operation = OperationType.MANAGE)
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        rbacService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/groups/{groupId}")
    @RequiresPermission(resource = ResourceType.GROUP, operation = OperationType.MANAGE)
    public ResponseEntity<?> assignGroupToUser(@PathVariable Long userId, @PathVariable Long groupId) {
        rbacService.assignGroupToUser(userId, groupId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/permissions")
    @RequiresPermission(resource = ResourceType.PERMISSION, operation = OperationType.MANAGE)
    public ResponseEntity<Void> addPermissionOverride(
            @PathVariable Long userId,
            @RequestParam String permission,
            @RequestParam boolean grant) {
        rbacService.addPermissionOverride(userId, permission, grant);
        return ResponseEntity.ok().build();
    }
}
