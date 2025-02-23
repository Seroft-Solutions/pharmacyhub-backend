package com.pharmacyhub.controller;

import com.pharmacyhub.security.dto.GroupDTO;
import com.pharmacyhub.security.dto.PermissionDTO;
import com.pharmacyhub.security.dto.RoleDTO;
import com.pharmacyhub.security.service.RBACService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
public class RBACController {
    private final RBACService rbacService;

    @PostMapping("/roles")
    @PreAuthorize("hasPermission('ROLE', 'CREATE')")
    public ResponseEntity<?> createRole(@RequestBody RoleDTO roleDTO) {
        return ResponseEntity.ok(rbacService.createRole(roleDTO));
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasPermission('PERMISSION', 'CREATE')")
    public ResponseEntity<?> createPermission(@RequestBody PermissionDTO permissionDTO) {
        return ResponseEntity.ok(rbacService.createPermission(permissionDTO));
    }

    @PostMapping("/groups")
    @PreAuthorize("hasPermission('GROUP', 'CREATE')")
    public ResponseEntity<?> createGroup(@RequestBody GroupDTO groupDTO) {
        return ResponseEntity.ok(rbacService.createGroup(groupDTO));
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasPermission('ROLE', 'MANAGE')")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        rbacService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/groups/{groupId}")
    @PreAuthorize("hasPermission('GROUP', 'MANAGE')")
    public ResponseEntity<?> assignGroupToUser(@PathVariable Long userId, @PathVariable Long groupId) {
        rbacService.assignGroupToUser(userId, groupId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/permissions")
    @PreAuthorize("hasPermission('PERMISSION', 'MANAGE')")
    public ResponseEntity<?> addPermissionOverride(
            @PathVariable Long userId,
            @RequestParam String permission,
            @RequestParam boolean grant) {
        rbacService.addPermissionOverride(userId, permission, grant);
        return ResponseEntity.ok().build();
    }
}