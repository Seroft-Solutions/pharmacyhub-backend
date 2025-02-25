package com.pharmacyhub.security.service;

import com.pharmacyhub.config.BaseIntegrationTest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

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
        rolesRepository.deleteAll();
        permissionRepository.deleteAll();
        groupRepository.deleteAll();

        // Create roles
        adminRole = TestDataBuilder.createRole(RoleEnum.ADMIN, 1);
        userRole = TestDataBuilder.createRole(RoleEnum.USER, 5);
        
        // Save roles
        adminRole = rolesRepository.save(adminRole);
        userRole = rolesRepository.save(userRole);
        
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
    @WithMockUser(roles = {"ADMIN"})
    void testCreatePermission() {
        // Create permission DTO
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("MANAGE_CONNECTIONS");
        permissionDTO.setDescription("Permission to manage connections");
        permissionDTO.setResourceType(ResourceType.CONNECTION);
        permissionDTO.setOperationType(OperationType.MANAGE);
        permissionDTO.setRequiresApproval(false);
        
        when(phMapper.getPermission(permissionDTO)).thenReturn(
            Permission.builder()
                .name("MANAGE_CONNECTIONS")
                .description("Permission to manage connections")
                .resourceType(ResourceType.CONNECTION)
                .operationType(OperationType.MANAGE)
                .requiresApproval(false)
                .build()
        );
        
        // Create permission
        PermissionDTO permission = rbacService.createPermission(permissionDTO);
        
        // Verify permission was created
        assertNotNull(permission);
        assertEquals("MANAGE_CONNECTIONS", permission.getName());
        assertEquals(ResourceType.CONNECTION, permission.getResourceType());
        assertEquals(OperationType.MANAGE, permission.getOperationType());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAssignRoleToUser() {
        // Assign role to user
        rbacService.assignRoleToUser(regularUser.getId(), adminRole.getId());
        
        // Verify role was assigned
        User updatedUser = userRepository.findById(regularUser.getId()).get();
        assertTrue(updatedUser.getRoles().contains(adminRole));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateGroupAndAssignToUser() {
        // Create group DTO
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setName("TestGroup");
        groupDTO.setDescription("Test group description");
        Set<Long> roleIds = new HashSet<>();
        roleIds.add(adminRole.getId());
        groupDTO.setRoleIds(roleIds);
        
        when(phMapper.getGroup(groupDTO)).thenReturn(
            Group.builder()
                .name("TestGroup")
                .description("Test group description")
                .roles(Set.of(adminRole))
                .build()
        );
        
        // Create group
        Group group = rbacService.createGroup(groupDTO);
        
        // Verify group was created
        assertNotNull(group);
        assertEquals("TestGroup", group.getName());
        
        // Assign group to user
        rbacService.assignGroupToUser(regularUser.getId(), group.getId());
        
        // Verify group was assigned
        User updatedUser = userRepository.findById(regularUser.getId()).get();
        assertTrue(updatedUser.getGroups().contains(group));
        
        // Check effective permissions
        Set<Permission> userPermissions = rbacService.getUserEffectivePermissions(regularUser.getId());
        assertEquals(1, userPermissions.size());
        assertTrue(userPermissions.contains(viewPharmacistPermission));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testPermissionDeniedForNonAdmin() {
        // Create permission DTO
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("TEST_PERMISSION");
        
        // Attempt to create permission as regular user - should throw AccessDeniedException
        assertThrows(AccessDeniedException.class, () -> {
            rbacService.createPermission(permissionDTO);
        });
    }
}
