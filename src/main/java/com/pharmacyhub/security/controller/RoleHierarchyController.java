package com.pharmacyhub.security.controller;

import com.pharmacyhub.security.annotation.RequiresPermission;
import com.pharmacyhub.security.service.RoleHierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rbac/roles/hierarchy")
@RequiredArgsConstructor
public class RoleHierarchyController {
    private final RoleHierarchyService roleHierarchyService;

    @PostMapping("/{parentRoleId}/children/{childRoleId}")
    @RequiresPermission(resource = "ROLE", operation = "MANAGE")
    public ResponseEntity<?> addChildRole(
            @PathVariable Long parentRoleId,
            @PathVariable Long childRoleId) {
        roleHierarchyService.addChildRole(parentRoleId, childRoleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{parentRoleId}/children/{childRoleId}")
    @RequiresPermission(resource = "ROLE", operation = "MANAGE")
    public ResponseEntity<?> removeChildRole(
            @PathVariable Long parentRoleId,
            @PathVariable Long childRoleId) {
        roleHierarchyService.removeChildRole(parentRoleId, childRoleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roleId}/children")
    @RequiresPermission(resource = "ROLE", operation = "READ")
    public ResponseEntity<?> getAllChildRoles(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleHierarchyService.getAllChildRoles(roleId));
    }

    @GetMapping("/precedence")
    @RequiresPermission(resource = "ROLE", operation = "READ")
    public ResponseEntity<?> getRolesByPrecedence() {
        return ResponseEntity.ok(roleHierarchyService.getRolesByPrecedence());
    }
}