package com.pharmacyhub.security.initializer;

import com.pharmacyhub.security.domain.*;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer {
    private final RolesRepository rolesRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;

    @PostConstruct
    @Transactional
    public void init() {
        if (rolesRepository.count() > 0) {
            log.info("Roles already initialized");
            return;
        }

        log.info("Initializing default roles and permissions");
        initializePermissions();
        initializeRoles();
        initializeGroups();
    }

    private void initializePermissions() {
        // Pharmacist Permissions
        createPermission("VIEW_PHARMACY_INVENTORY", "View pharmacy inventory", ResourceType.INVENTORY, OperationType.READ, false);
        createPermission("MANAGE_PRESCRIPTIONS", "Manage prescriptions", ResourceType.PRESCRIPTION, OperationType.MANAGE, false);
        createPermission("VIEW_PATIENT_HISTORY", "View patient prescription history", ResourceType.PRESCRIPTION, OperationType.READ, false);

        // Pharmacy Manager Permissions
        createPermission("MANAGE_INVENTORY", "Manage pharmacy inventory", ResourceType.INVENTORY, OperationType.MANAGE, false);
        createPermission("VIEW_SALES_REPORTS", "View sales reports", ResourceType.ORDER, OperationType.READ, false);
        createPermission("MANAGE_STAFF", "Manage pharmacy staff", ResourceType.PHARMACIST, OperationType.MANAGE, true);

        // Proprietor Permissions
        createPermission("VIEW_ALL_BRANCHES", "View all pharmacy branches", ResourceType.INVENTORY, OperationType.VIEW_ALL, false);
        createPermission("MANAGE_BRANCHES", "Manage pharmacy branches", ResourceType.PHARMACY_MANAGER, OperationType.MANAGE, false);
        createPermission("VIEW_FINANCIAL_REPORTS", "View financial reports", ResourceType.ORDER, OperationType.EXPORT, false);

        // Salesman Permissions
        createPermission("CREATE_ORDERS", "Create new orders", ResourceType.ORDER, OperationType.CREATE, false);
        createPermission("VIEW_ORDERS", "View orders", ResourceType.ORDER, OperationType.READ, false);
        createPermission("UPDATE_ORDER_STATUS", "Update order status", ResourceType.ORDER, OperationType.UPDATE, false);

        // Admin Permissions
        createPermission("MANAGE_ROLES", "Manage roles", ResourceType.ROLE, OperationType.MANAGE, true);
        createPermission("MANAGE_PERMISSIONS", "Manage permissions", ResourceType.PERMISSION, OperationType.MANAGE, true);
        createPermission("MANAGE_GROUPS", "Manage groups", ResourceType.GROUP, OperationType.MANAGE, true);
        createPermission("VIEW_AUDIT_LOGS", "View audit logs", ResourceType.AUDIT_LOG, OperationType.VIEW_ALL, false);
        createPermission("MANAGE_SYSTEM_SETTINGS", "Manage system settings", ResourceType.SYSTEM_SETTING, OperationType.MANAGE, true);
    }

    private void initializeRoles() {
        // Create Pharmacist Role
        Role pharmacistRole = createRole("PHARMACIST", "Pharmacist role", 3, 
            "VIEW_PHARMACY_INVENTORY",
            "MANAGE_PRESCRIPTIONS",
            "VIEW_PATIENT_HISTORY"
        );

        // Create Pharmacy Manager Role
        Role managerRole = createRole("PHARMACY_MANAGER", "Pharmacy Manager role", 2,
            "MANAGE_INVENTORY",
            "VIEW_SALES_REPORTS",
            "MANAGE_STAFF"
        );

        // Create Proprietor Role
        Role proprietorRole = createRole("PROPRIETOR", "Proprietor role", 1,
            "VIEW_ALL_BRANCHES",
            "MANAGE_BRANCHES",
            "VIEW_FINANCIAL_REPORTS"
        );

        // Create Salesman Role
        Role salesmanRole = createRole("SALESMAN", "Salesman role", 4,
            "CREATE_ORDERS",
            "VIEW_ORDERS",
            "UPDATE_ORDER_STATUS"
        );

        // Create Admin Role
        Role adminRole = createRole("ADMIN", "Administrator role", 0,
            "MANAGE_ROLES",
            "MANAGE_PERMISSIONS",
            "MANAGE_GROUPS",
            "VIEW_AUDIT_LOGS",
            "MANAGE_SYSTEM_SETTINGS"
        );

        // Set up role hierarchy
        managerRole.setChildRoles(Set.of(pharmacistRole));
        proprietorRole.setChildRoles(Set.of(managerRole));
        adminRole.setChildRoles(Set.of(proprietorRole));

        rolesRepository.saveAll(Arrays.asList(pharmacistRole, managerRole, proprietorRole, salesmanRole, adminRole));
    }

    private void initializeGroups() {
        // Create Staff Group
        createGroup("PHARMACY_STAFF", "General pharmacy staff group",
            "PHARMACIST",
            "SALESMAN"
        );

        // Create Management Group
        createGroup("PHARMACY_MANAGEMENT", "Pharmacy management group",
            "PHARMACY_MANAGER",
            "PROPRIETOR"
        );

        // Create System Administrators Group
        createGroup("SYSTEM_ADMINISTRATORS", "System administrators group",
            "ADMIN"
        );
    }

    private Permission createPermission(String name, String description, ResourceType resourceType, 
                                     OperationType operationType, boolean requiresApproval) {
        Permission permission = Permission.builder()
            .name(name)
            .description(description)
            .resourceType(resourceType)
            .operationType(operationType)
            .requiresApproval(requiresApproval)
            .build();
        return permissionRepository.save(permission);
    }

    private Role createRole(String name, String description, int precedence, String... permissionNames) {
        Set<Permission> permissions = Arrays.stream(permissionNames)
            .map(permissionName -> permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName)))
            .collect(java.util.stream.Collectors.toSet());

        Role role = Role.builder()
            .name(name)
            .description(description)
            .permissions(permissions)
            .precedence(precedence)
            .system(true)
            .childRoles(new HashSet<>())
            .build();

        return rolesRepository.save(role);
    }

    private Group createGroup(String name, String description, String... roleNames) {
        Set<Role> roles = Arrays.stream(roleNames)
            .map(roleName -> rolesRepository.findByName(roleName)
                                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
            .collect(java.util.stream.Collectors.toSet());

        Group group = Group.builder()
            .name(name)
            .description(description)
            .roles(roles)
            .build();

        return groupRepository.save(group);
    }
}