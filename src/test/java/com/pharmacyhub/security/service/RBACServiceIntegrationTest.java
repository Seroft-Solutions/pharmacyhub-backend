package com.pharmacyhub.security.service;

import com.pharmacyhub.config.TestConfig;
import com.pharmacyhub.security.config.TestSecurityConfig;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.OperationType;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.ResourceType;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import({TestConfig.class, TestSecurityConfig.class})
@WithMockUser(username = "admin", roles = {"ADMIN"}, authorities = {
    "PERMISSION_ROLE_ASSIGN",
    "PERMISSION_GROUP_ASSIGN",
    "PERMISSION_PERMISSION_MANAGE",
    "PERMISSION_USER_READ"
})
public class RBACServiceIntegrationTest {
    // Existing code remains the same...
    // ...

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

    private User testUser;
    private Role adminRole;
    private Role pharmacistRole;
    private Permission createPrescriptionPermission;
    private Permission viewPrescriptionPermission;
    private Permission managePrescriptionPermission;
    private Group pharmacyGroup;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        rolesRepository.deleteAll();
        permissionRepository.deleteAll();
        groupRepository.deleteAll();
        
        // Create test permissions
        createPrescriptionPermission = Permission.builder()
                .name("CREATE_PRESCRIPTION")
                .description("Permission to create prescriptions")
                .resourceType(ResourceType.PRESCRIPTION)
                .operationType(OperationType.CREATE)
                .requiresApproval(false)
                .build();
        permissionRepository.save(createPrescriptionPermission);

        viewPrescriptionPermission = Permission.builder()
                .name("VIEW_PRESCRIPTION")
                .description("Permission to view prescriptions")
                .resourceType(ResourceType.PRESCRIPTION)
                .operationType(OperationType.READ)
                .requiresApproval(false)
                .build();
        permissionRepository.save(viewPrescriptionPermission);

        managePrescriptionPermission = Permission.builder()
                .name("MANAGE_PRESCRIPTION")
                .description("Permission to manage prescriptions")
                .resourceType(ResourceType.PRESCRIPTION)
                .operationType(OperationType.MANAGE)
                .requiresApproval(true)
                .build();
        permissionRepository.save(managePrescriptionPermission);

        // Create test roles
        Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(createPrescriptionPermission);
        adminPermissions.add(viewPrescriptionPermission);
        adminPermissions.add(managePrescriptionPermission);

        adminRole = Role.builder()
                .name(com.pharmacyhub.constants.RoleEnum.ADMIN)
                .description("Administrator role")
                .permissions(adminPermissions)
                .precedence(1)
                .system(true)
                .childRoles(new HashSet<>())
                .build();
        rolesRepository.save(adminRole);

        Set<Permission> pharmacistPermissions = new HashSet<>();
        pharmacistPermissions.add(viewPrescriptionPermission);

        pharmacistRole = Role.builder()
                .name(com.pharmacyhub.constants.RoleEnum.PHARMACIST)
                .description("Pharmacist role")
                .permissions(pharmacistPermissions)
                .precedence(2)
                .system(true)
                .childRoles(new HashSet<>())
                .build();
        rolesRepository.save(pharmacistRole);

        // Create test group
        Set<Role> groupRoles = new HashSet<>();
        groupRoles.add(pharmacistRole);

        pharmacyGroup = Group.builder()
                .name("CENTRAL_PHARMACY")
                .description("Central pharmacy staff")
                .roles(groupRoles)
                .build();
        groupRepository.save(pharmacyGroup);

        // Create test user
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(pharmacistRole);

        Set<Group> userGroups = new HashSet<>();
        userGroups.add(pharmacyGroup);

        testUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .emailAddress("test.user@pharmacyhub.com")
                .password("password")
                .roles(userRoles)
                .groups(userGroups)
                .permissionOverrides(new HashSet<>())
                .active(true)
                .verified(true)
                .accountNonLocked(true)
                .registered(true)
                .build();
        userRepository.save(testUser);
    }

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
        rolesRepository.deleteAll();
        permissionRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    public void testGetUserEffectivePermissions() {
        Set<Permission> permissions = rbacService.getUserEffectivePermissions(testUser.getId());
        assertTrue(permissions.contains(viewPrescriptionPermission));
        assertFalse(permissions.contains(createPrescriptionPermission));
        assertFalse(permissions.contains(managePrescriptionPermission));
    }

    @Test
    public void testAddAndRemoveRoleFromUser() {
        rbacService.assignRoleToUser(testUser.getId(), adminRole.getId());
        
        Set<Permission> permissions = rbacService.getUserEffectivePermissions(testUser.getId());
        assertTrue(permissions.contains(createPrescriptionPermission));
        assertTrue(permissions.contains(viewPrescriptionPermission));
        assertTrue(permissions.contains(managePrescriptionPermission));
        
        rbacService.removeRoleFromUser(testUser.getId(), adminRole.getId());
        
        permissions = rbacService.getUserEffectivePermissions(testUser.getId());
        assertFalse(permissions.contains(createPrescriptionPermission));
        assertTrue(permissions.contains(viewPrescriptionPermission));
        assertFalse(permissions.contains(managePrescriptionPermission));
    }

    @Test
    public void testPermissionOverrides() {
        rbacService.addPermissionOverride(testUser.getId(), "CREATE_PRESCRIPTION", true);
        
        Set<Permission> permissions = rbacService.getUserEffectivePermissions(testUser.getId());
        assertTrue(permissions.contains(createPrescriptionPermission));
        
        rbacService.addPermissionOverride(testUser.getId(), "VIEW_PRESCRIPTION", false);
        
        permissions = rbacService.getUserEffectivePermissions(testUser.getId());
        assertFalse(permissions.contains(viewPrescriptionPermission));
        
        rbacService.removePermissionOverride(testUser.getId(), "-VIEW_PRESCRIPTION");
        
        permissions = rbacService.getUserEffectivePermissions(testUser.getId());
        assertTrue(permissions.contains(viewPrescriptionPermission));
    }

    @Test
    public void testAccessValidation() {
        assertTrue(rbacService.validateAccess(testUser.getId(), "PRESCRIPTION", "READ", 1L));
        assertFalse(rbacService.validateAccess(testUser.getId(), "PRESCRIPTION", "CREATE", 1L));
        
        rbacService.assignRoleToUser(testUser.getId(), adminRole.getId());
        
        assertTrue(rbacService.validateAccess(testUser.getId(), "PRESCRIPTION", "READ", 1L));
        assertTrue(rbacService.validateAccess(testUser.getId(), "PRESCRIPTION", "CREATE", 1L));
    }

    @Test
    public void testUserQueries() {
        User adminUser = User.builder()
                .firstName("Admin")
                .lastName("User")
                .emailAddress("admin@pharmacyhub.com")
                .password("password")
                .roles(Set.of(adminRole))
                .permissionOverrides(new HashSet<>())
                .active(true)
                .verified(true)
                .accountNonLocked(true)
                .registered(true)
                .build();
        userRepository.save(adminUser);
        
        List<User> adminUsers = rbacService.getUsersByRole(adminRole.getName());
        assertEquals(1, adminUsers.size());
        assertEquals(adminUser.getId(), adminUsers.get(0).getId());
        
        List<User> pharmacistUsers = rbacService.getUsersByRole(pharmacistRole.getName());
        assertEquals(1, pharmacistUsers.size());
        assertEquals(testUser.getId(), pharmacistUsers.get(0).getId());
        
        List<User> pharmacyGroupUsers = rbacService.getUsersByGroup(pharmacyGroup.getName());
        assertEquals(1, pharmacyGroupUsers.size());
        assertEquals(testUser.getId(), pharmacyGroupUsers.get(0).getId());
    }
}
