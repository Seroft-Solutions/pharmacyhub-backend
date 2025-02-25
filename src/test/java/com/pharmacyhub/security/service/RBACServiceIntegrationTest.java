package com.pharmacyhub.security.service;

import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.config.TestDatabaseSetup;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.engine.PHMapper;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.dto.GroupDTO;
import com.pharmacyhub.security.dto.PermissionDTO;
import com.pharmacyhub.security.dto.RoleDTO;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import com.pharmacyhub.util.TestDataBuilder;
import com.pharmacyhub.util.TestSecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RBACServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RBACService rbacService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private TestDatabaseSetup testDatabaseSetup;

    @MockBean
    private PHMapper phMapper;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role userRole;
    private Permission viewPharmacistPermission;

    @BeforeEach
    void setUp() {
        // Clear repositories
        userRepository.deleteAll();
        testDatabaseSetup.clearAllRoles();
        permissionRepository.deleteAll();
        groupRepository.deleteAll();

        // Create roles using the test utility
        adminRole = testDatabaseSetup.getOrCreateRole(RoleEnum.ADMIN, 1);
        userRole = testDatabaseSetup.getOrCreateRole(RoleEnum.USER, 5);
        
        // Create permissions
        viewPharmacistPermission = Permission.builder()
                .name("VIEW_PHARMACIST")
                .description("Permission to view pharmacist")
                .resourceType(ResourceType.PHARMACIST)
                .operationType(OperationType.READ)
                .requiresApproval(false)
                .build();
        viewPharmacistPermission = permissionRepository.save(viewPharmacistPermission);
        
        // Add permission to admin role
        Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(viewPharmacistPermission);
        adminRole.setPermissions(adminPermissions);
        adminRole = rolesRepository.save(adminRole);
        
        // Create users
        adminUser = TestDataBuilder.createUser("admin@pharmacyhub.pk", "password", UserType.ADMIN);
        regularUser = TestDataBuilder.createUser("user@pharmacyhub.pk", "password", UserType.PHARMACIST);
        
        // Add roles to users
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRoles(adminRoles);
        
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        regularUser.setRoles(userRoles);
        
        // Save users
        adminUser = userRepository.save(adminUser);
        regularUser = userRepository.save(regularUser);
    }

    // Remove the tearDown() method since it's already defined in BaseIntegrationTest with public access
    // The parent tearDown() method will be called automatically

    @Test
    void testGetUserEffectivePermissions() {
        // Get user permissions
        Set<Permission> adminPermissions = rbacService.getUserEffectivePermissions(adminUser.getId());
        Set<Permission> userPermissions = rbacService.getUserEffectivePermissions(regularUser.getId());
        
        // Check admin permissions
        assertEquals(1, adminPermissions.size());
        assertTrue(adminPermissions.contains(viewPharmacistPermission));
        
        // Check user permissions
        assertEquals(0, userPermissions.size());
    }

    @Test
    void testCreatePermission() {
        // Setup security context with admin user
        TestSecurityUtils.setupTestSecurityContext(RoleEnum.ADMIN);
        
        // Create permission DTO
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("MANAGE_CONNECTIONS");
        permissionDTO.setDescription("Permission to manage connections");
        permissionDTO.setResourceType(ResourceType.CONNECTION);
        permissionDTO.setOperationType(OperationType.MANAGE);
        permissionDTO.setRequiresApproval(false);
        
        Permission managementPermission = Permission.builder()
            .name("MANAGE_CONNECTIONS")
            .description("Permission to manage connections")
            .resourceType(ResourceType.CONNECTION)
            .operationType(OperationType.MANAGE)
            .requiresApproval(false)
            .build();
            
        when(phMapper.getPermission(permissionDTO)).thenReturn(managementPermission);
        when(phMapper.getPermissionDTO(managementPermission)).thenReturn(permissionDTO);
        
        // Create permission
        PermissionDTO permission = rbacService.createPermission(permissionDTO);
        
        // Verify permission was created
        assertNotNull(permission);
        assertEquals("MANAGE_CONNECTIONS", permission.getName());
        assertEquals(ResourceType.CONNECTION, permission.getResourceType());
        assertEquals(OperationType.MANAGE, permission.getOperationType());
    }

    @Test
    void testAssignRoleToUser() {
        // Setup security context with admin user
        TestSecurityUtils.setupTestSecurityContext(RoleEnum.ADMIN);
        
        // Assign role to user
        rbacService.assignRoleToUser(regularUser.getId(), adminRole.getId());
        
        // Verify role was assigned
        User updatedUser = userRepository.findById(regularUser.getId()).get();
        assertTrue(updatedUser.getRoles().contains(adminRole));
    }

    @Test
    void testCreateGroupAndAssignToUser() {
        // Setup security context with admin user
        TestSecurityUtils.setupTestSecurityContext(RoleEnum.ADMIN);
        
        // Create group DTO
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setName("TestGroup");
        groupDTO.setDescription("Test group description");
        Set<Long> roleIds = new HashSet<>();
        roleIds.add(adminRole.getId());
        groupDTO.setRoleIds(roleIds);
        
        Group group = Group.builder()
            .name("TestGroup")
            .description("Test group description")
            .roles(Set.of(adminRole))
            .build();
            
        when(phMapper.getGroup(groupDTO)).thenReturn(group);
        
        // Create group
        Group createdGroup = rbacService.createGroup(groupDTO);
        
        // Verify group was created
        assertNotNull(createdGroup);
        assertEquals("TestGroup", createdGroup.getName());
        
        // Assign group to user
        rbacService.assignGroupToUser(regularUser.getId(), createdGroup.getId());
        
        // Verify group was assigned
        User updatedUser = userRepository.findById(regularUser.getId()).get();
        assertTrue(updatedUser.getGroups().contains(createdGroup));
        
        // Check effective permissions
        Set<Permission> userPermissions = rbacService.getUserEffectivePermissions(regularUser.getId());
        assertEquals(1, userPermissions.size());
        assertTrue(userPermissions.contains(viewPharmacistPermission));
    }

    @Test
    void testPermissionDeniedForNonAdmin() {
        // Setup security context with regular user
        TestSecurityUtils.setupTestSecurityContext(RoleEnum.USER);
        
        // Create permission DTO
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("TEST_PERMISSION");
        
        // Attempt to create permission as regular user - should throw AccessDeniedException
        assertThrows(AccessDeniedException.class, () -> {
            rbacService.createPermission(permissionDTO);
        });
    }
}