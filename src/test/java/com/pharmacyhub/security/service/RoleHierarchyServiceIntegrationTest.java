package com.pharmacyhub.security.service;

import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.exception.RBACException;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleHierarchyServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoleHierarchyService roleHierarchyService;

    @Autowired
    private RolesRepository rolesRepository;

    private Role adminRole;
    private Role proprietorRole;
    private Role pharmacistRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Clear repository
        rolesRepository.deleteAll();
        
        // Create roles with different precedence levels
        adminRole = Role.builder()
                .name(RoleEnum.ADMIN)
                .description("Admin role")
                .precedence(1)
                .system(true)
                .permissions(new HashSet<>())
                .childRoles(new HashSet<>())
                .build();
        adminRole = rolesRepository.save(adminRole);
        
        proprietorRole = Role.builder()
                .name(RoleEnum.PROPRIETOR)
                .description("Proprietor role")
                .precedence(2)
                .system(true)
                .permissions(new HashSet<>())
                .childRoles(new HashSet<>())
                .build();
        proprietorRole = rolesRepository.save(proprietorRole);
        
        pharmacistRole = Role.builder()
                .name(RoleEnum.PHARMACIST)
                .description("Pharmacist role")
                .precedence(3)
                .system(true)
                .permissions(new HashSet<>())
                .childRoles(new HashSet<>())
                .build();
        pharmacistRole = rolesRepository.save(pharmacistRole);
        
        userRole = Role.builder()
                .name(RoleEnum.USER)
                .description("User role")
                .precedence(5)
                .system(true)
                .permissions(new HashSet<>())
                .childRoles(new HashSet<>())
                .build();
        userRole = rolesRepository.save(userRole);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAddChildRole() {
        // Add child role
        roleHierarchyService.addChildRole(adminRole.getId(), proprietorRole.getId());
        roleHierarchyService.addChildRole(proprietorRole.getId(), pharmacistRole.getId());
        
        // Get admin role with child roles
        Role admin = rolesRepository.findByIdWithChildRoles(adminRole.getId());
        
        // Verify hierarchy
        assertEquals(1, admin.getChildRoles().size());
        assertTrue(admin.getChildRoles().stream().anyMatch(role -> role.getId().equals(proprietorRole.getId())));
        
        // Get proprietor role with child roles
        Role proprietor = rolesRepository.findByIdWithChildRoles(proprietorRole.getId());
        
        // Verify hierarchy
        assertEquals(1, proprietor.getChildRoles().size());
        assertTrue(proprietor.getChildRoles().stream().anyMatch(role -> role.getId().equals(pharmacistRole.getId())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllChildRoles() {
        // Create hierarchy: admin -> proprietor -> pharmacist -> user
        roleHierarchyService.addChildRole(adminRole.getId(), proprietorRole.getId());
        roleHierarchyService.addChildRole(proprietorRole.getId(), pharmacistRole.getId());
        roleHierarchyService.addChildRole(pharmacistRole.getId(), userRole.getId());
        
        // Get all child roles for admin
        Set<Role> allChildRoles = roleHierarchyService.getAllChildRoles(adminRole.getId());
        
        // Verify returned roles
        assertEquals(3, allChildRoles.size());
        assertTrue(allChildRoles.stream().anyMatch(role -> role.getId().equals(proprietorRole.getId())));
        assertTrue(allChildRoles.stream().anyMatch(role -> role.getId().equals(pharmacistRole.getId())));
        assertTrue(allChildRoles.stream().anyMatch(role -> role.getId().equals(userRole.getId())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testRemoveChildRole() {
        // Create hierarchy: admin -> proprietor
        roleHierarchyService.addChildRole(adminRole.getId(), proprietorRole.getId());
        
        // Verify hierarchy
        Role admin = rolesRepository.findByIdWithChildRoles(adminRole.getId());
        assertEquals(1, admin.getChildRoles().size());
        
        // Remove child role
        roleHierarchyService.removeChildRole(adminRole.getId(), proprietorRole.getId());
        
        // Verify hierarchy after removal
        admin = rolesRepository.findByIdWithChildRoles(adminRole.getId());
        assertEquals(0, admin.getChildRoles().size());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetRolesByPrecedence() {
        // Get roles by precedence
        List<Role> roles = roleHierarchyService.getRolesByPrecedence();
        
        // Verify roles are ordered by precedence
        assertEquals(4, roles.size());
        assertEquals(adminRole.getId(), roles.get(0).getId()); // precedence 1
        assertEquals(proprietorRole.getId(), roles.get(1).getId()); // precedence 2
        assertEquals(pharmacistRole.getId(), roles.get(2).getId()); // precedence 3
        assertEquals(userRole.getId(), roles.get(3).getId()); // precedence 5
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCircularDependencyDetection() {
        // Create hierarchy: admin -> proprietor -> pharmacist
        roleHierarchyService.addChildRole(adminRole.getId(), proprietorRole.getId());
        roleHierarchyService.addChildRole(proprietorRole.getId(), pharmacistRole.getId());
        
        // Try to create circular dependency: pharmacist -> admin
        Exception exception = assertThrows(RBACException.class, () -> {
            roleHierarchyService.addChildRole(pharmacistRole.getId(), adminRole.getId());
        });
        
        // Verify exception is about circular dependency
        assertTrue(exception.getMessage().contains("Invalid role hierarchy"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testInvalidPrecedence() {
        // Try to add child role with lower precedence as parent
        Exception exception = assertThrows(RBACException.class, () -> {
            roleHierarchyService.addChildRole(pharmacistRole.getId(), adminRole.getId());
        });
        
        // Verify exception is about precedence
        assertTrue(exception.getMessage().contains("Child role must have lower precedence"));
    }
}
