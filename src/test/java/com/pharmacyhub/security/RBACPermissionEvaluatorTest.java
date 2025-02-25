package com.pharmacyhub.security;

import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.config.TestDatabaseSetup;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.evaluator.PHPermissionEvaluator;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import com.pharmacyhub.security.service.RBACService;
import com.pharmacyhub.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RBACPermissionEvaluatorTest extends BaseIntegrationTest {

    @Autowired
    private PHPermissionEvaluator permissionEvaluator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RBACService rbacService;
    
    @Autowired
    private TestDatabaseSetup testDatabaseSetup;

    private User adminUser;
    private User pharmacistUser;
    private Role adminRole;
    private Role pharmacistRole;
    private Permission viewPharmacistPermission;
    private Permission manageConnectionsPermission;

    @BeforeEach
    void setUp() {
        // Clear repositories
        userRepository.deleteAll();
        testDatabaseSetup.clearAllRoles();
        permissionRepository.deleteAll();
        
        // Create permissions
        viewPharmacistPermission = Permission.builder()
                .name("VIEW_PHARMACIST")
                .description("Permission to view pharmacist")
                .resourceType(ResourceType.PHARMACIST)
                .operationType(OperationType.READ)
                .requiresApproval(false)
                .build();
        viewPharmacistPermission = permissionRepository.save(viewPharmacistPermission);
        
        manageConnectionsPermission = Permission.builder()
                .name("MANAGE_CONNECTIONS")
                .description("Permission to manage connections")
                .resourceType(ResourceType.CONNECTION)
                .operationType(OperationType.MANAGE)
                .requiresApproval(false)
                .build();
        manageConnectionsPermission = permissionRepository.save(manageConnectionsPermission);
        
        // Create roles using the test utility
        adminRole = testDatabaseSetup.getOrCreateRole(RoleEnum.ADMIN, 1);
        Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(viewPharmacistPermission);
        adminPermissions.add(manageConnectionsPermission);
        adminRole.setPermissions(adminPermissions);
        adminRole = rolesRepository.save(adminRole);
        
        pharmacistRole = testDatabaseSetup.getOrCreateRole(RoleEnum.PHARMACIST, 3);
        Set<Permission> pharmacistPermissions = new HashSet<>();
        pharmacistPermissions.add(viewPharmacistPermission);
        pharmacistRole.setPermissions(pharmacistPermissions);
        pharmacistRole = rolesRepository.save(pharmacistRole);
        
        // Create users
        adminUser = TestDataBuilder.createUser("admin@pharmacyhub.pk", 
                passwordEncoder.encode("password"), UserType.ADMIN);
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRoles(adminRoles);
        adminUser = userRepository.save(adminUser);
        
        pharmacistUser = TestDataBuilder.createUser("pharmacist@pharmacyhub.pk", 
                passwordEncoder.encode("password"), UserType.PHARMACIST);
        Set<Role> pharmacistRoles = new HashSet<>();
        pharmacistRoles.add(pharmacistRole);
        pharmacistUser.setRoles(pharmacistRoles);
        pharmacistUser = userRepository.save(pharmacistUser);
    }

    @Test
    void testAdminHasPermission() {
        // Create authentication with admin user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                adminUser, null, adminUser.getAuthorities());
        
        // Test permissions
        assertTrue(permissionEvaluator.hasPermission(auth, "PHARMACIST", "READ"));
        assertTrue(permissionEvaluator.hasPermission(auth, "CONNECTION", "MANAGE"));
    }

    @Test
    void testPharmacistHasLimitedPermissions() {
        // Create authentication with pharmacist user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                pharmacistUser, null, pharmacistUser.getAuthorities());
        
        // Test permissions
        assertTrue(permissionEvaluator.hasPermission(auth, "PHARMACIST", "READ"));
        assertFalse(permissionEvaluator.hasPermission(auth, "CONNECTION", "MANAGE"));
    }

    @Test
    void testPermissionWithTargetId() {
        // Create authentication with admin user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                adminUser, null, adminUser.getAuthorities());
        
        // Test permission with target ID
        assertTrue(permissionEvaluator.hasPermission(
                auth, 1L, "PHARMACIST", "READ"));
    }

    @Test
    void testPermissionNullArgs() {
        // Create authentication with admin user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                adminUser, null, adminUser.getAuthorities());
        
        // Test with null arguments
        assertFalse(permissionEvaluator.hasPermission(auth, null, "READ"));
        assertFalse(permissionEvaluator.hasPermission(null, "PHARMACIST", "READ"));
        assertFalse(permissionEvaluator.hasPermission(auth, "PHARMACIST", null));
    }

    @Test
    void testRBACServiceGetUserEffectivePermissions() {
        // Get user effective permissions
        Set<Permission> adminPermissions = rbacService.getUserEffectivePermissions(adminUser.getId());
        Set<Permission> pharmacistPermissions = rbacService.getUserEffectivePermissions(pharmacistUser.getId());
        
        // Verify admin permissions
        assertEquals(2, adminPermissions.size());
        assertTrue(adminPermissions.contains(viewPharmacistPermission));
        assertTrue(adminPermissions.contains(manageConnectionsPermission));
        
        // Verify pharmacist permissions
        assertEquals(1, pharmacistPermissions.size());
        assertTrue(pharmacistPermissions.contains(viewPharmacistPermission));
        assertFalse(pharmacistPermissions.contains(manageConnectionsPermission));
    }
}