package com.pharmacyhub.security.initializer;

import com.pharmacyhub.security.domain.*;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import com.pharmacyhub.constants.RoleEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final RolesRepository rolesRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            if (rolesRepository.count() > 0) {
                log.info("Roles already initialized");
                return;
            }

            log.info("Initializing default roles and permissions");
            Map<String, Permission> permissions = initializePermissions();
            Map<RoleEnum, Role> roles = initializeRoles(permissions);
            initializeGroups(roles);
            
            log.info("Role initialization completed successfully");
        } catch (Exception e) {
            log.error("Error initializing roles: ", e);
        }
    }

    private Map<String, Permission> initializePermissions() {
        log.info("Initializing permissions...");
        Map<String, Permission> permissionMap = new HashMap<>();
        
        // Common permissions
        permissionMap.put("VIEW_PROFILE", createPermission("VIEW_PROFILE", 
            "Permission to view user profile", ResourceType.USER, OperationType.READ, false));
        permissionMap.put("UPDATE_PROFILE", createPermission("UPDATE_PROFILE", 
            "Permission to update user profile", ResourceType.USER, OperationType.UPDATE, false));
            
        // Pharmacist permissions
        permissionMap.put("VIEW_PHARMACY_INVENTORY", createPermission("VIEW_PHARMACY_INVENTORY", 
            "View pharmacy inventory", ResourceType.INVENTORY, OperationType.READ, false));
        permissionMap.put("MANAGE_PHARMACY_INVENTORY", createPermission("MANAGE_PHARMACY_INVENTORY", 
            "Manage pharmacy inventory", ResourceType.INVENTORY, OperationType.MANAGE, false));
            
        // Manager permissions
        permissionMap.put("MANAGE_PHARMACY", createPermission("MANAGE_PHARMACY", 
            "Manage pharmacy operations", ResourceType.PHARMACY, OperationType.MANAGE, false));
        permissionMap.put("VIEW_PHARMACY", createPermission("VIEW_PHARMACY", 
            "View pharmacy details", ResourceType.PHARMACY, OperationType.READ, false));
            
        // Proprietor permissions
        permissionMap.put("MANAGE_BUSINESS", createPermission("MANAGE_BUSINESS", 
            "Manage pharmacy business", ResourceType.BUSINESS, OperationType.MANAGE, false));
            
        // Salesman permissions
        permissionMap.put("PROCESS_SALES", createPermission("PROCESS_SALES", 
            "Process sales transactions", ResourceType.SALES, OperationType.CREATE, false));
        permissionMap.put("VIEW_SALES", createPermission("VIEW_SALES", 
            "View sales records", ResourceType.SALES, OperationType.READ, false));
            
        // Admin permissions
        permissionMap.put("MANAGE_USERS", createPermission("MANAGE_USERS", 
            "Manage system users", ResourceType.USER, OperationType.MANAGE, false));
        permissionMap.put("MANAGE_ROLES", createPermission("MANAGE_ROLES", 
            "Manage system roles", ResourceType.ROLE, OperationType.MANAGE, false));
        permissionMap.put("MANAGE_PERMISSIONS", createPermission("MANAGE_PERMISSIONS", 
            "Manage system permissions", ResourceType.PERMISSION, OperationType.MANAGE, false));
        
        return permissionMap;
    }

    private Map<RoleEnum, Role> initializeRoles(Map<String, Permission> permissions) {
        log.info("Initializing roles...");
        Map<RoleEnum, Role> roleMap = new HashMap<>();

        // Create USER role (lowest precedence - 100)
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(permissions.get("VIEW_PROFILE"));
        userPermissions.add(permissions.get("UPDATE_PROFILE"));
        
        Role userRole = Role.builder()
                .name(RoleEnum.USER)
                .description("Base user role with minimal permissions")
                .precedence(100)
                .permissions(userPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.USER, rolesRepository.save(userRole));

        // Create PHARMACIST role (precedence - 80)
        Set<Permission> pharmacistPermissions = new HashSet<>(userPermissions);
        pharmacistPermissions.add(permissions.get("VIEW_PHARMACY_INVENTORY"));
        
        Role pharmacistRole = Role.builder()
                .name(RoleEnum.PHARMACIST)
                .description("Pharmacist role with permissions to view inventory")
                .precedence(80)
                .permissions(pharmacistPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.PHARMACIST, rolesRepository.save(pharmacistRole));

        // Create PHARMACY_MANAGER role (precedence - 60)
        Set<Permission> managerPermissions = new HashSet<>(pharmacistPermissions);
        managerPermissions.add(permissions.get("MANAGE_PHARMACY_INVENTORY"));
        managerPermissions.add(permissions.get("MANAGE_PHARMACY"));
        
        Role managerRole = Role.builder()
                .name(RoleEnum.PHARMACY_MANAGER)
                .description("Pharmacy manager role for managing pharmacy operations")
                .precedence(60)
                .permissions(managerPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.PHARMACY_MANAGER, rolesRepository.save(managerRole));

        // Create PROPRIETOR role (precedence - 40)
        Set<Permission> proprietorPermissions = new HashSet<>(managerPermissions);
        proprietorPermissions.add(permissions.get("MANAGE_BUSINESS"));
        
        Role proprietorRole = Role.builder()
                .name(RoleEnum.PROPRIETOR)
                .description("Proprietor role for pharmacy business ownership")
                .precedence(40)
                .permissions(proprietorPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.PROPRIETOR, rolesRepository.save(proprietorRole));

        // Create SALESMAN role (precedence - 90)
        Set<Permission> salesmanPermissions = new HashSet<>(userPermissions);
        salesmanPermissions.add(permissions.get("PROCESS_SALES"));
        salesmanPermissions.add(permissions.get("VIEW_SALES"));
        
        Role salesmanRole = Role.builder()
                .name(RoleEnum.SALESMAN)
                .description("Salesman role for processing sales")
                .precedence(90)
                .permissions(salesmanPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.SALESMAN, rolesRepository.save(salesmanRole));

        // Create ADMIN role (precedence - 20)
        Set<Permission> adminPermissions = new HashSet<>();
        permissions.values().forEach(adminPermissions::add);
        
        Role adminRole = Role.builder()
                .name(RoleEnum.ADMIN)
                .description("Administrator role with full system access")
                .precedence(20)
                .permissions(adminPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.ADMIN, rolesRepository.save(adminRole));

        // Create SUPER_ADMIN role (precedence - 10)
        Role superAdminRole = Role.builder()
                .name(RoleEnum.SUPER_ADMIN)
                .description("Super administrator with highest privileges")
                .precedence(10)
                .permissions(adminPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.SUPER_ADMIN, rolesRepository.save(superAdminRole));

        return roleMap;
    }

    private void initializeGroups(Map<RoleEnum, Role> roles) {
        log.info("Initializing groups...");
        
        // Create Staff Group (Pharmacists, Salesmen)
        Set<Role> staffRoles = new HashSet<>();
        staffRoles.add(roles.get(RoleEnum.PHARMACIST));
        staffRoles.add(roles.get(RoleEnum.SALESMAN));
        
        Group staffGroup = Group.builder()
                .name("PHARMACY_STAFF")
                .description("Group for pharmacy staff members")
                .roles(staffRoles)
                .build();
        groupRepository.save(staffGroup);
        
        // Create Management Group (Managers, Proprietors)
        Set<Role> managementRoles = new HashSet<>();
        managementRoles.add(roles.get(RoleEnum.PHARMACY_MANAGER));
        managementRoles.add(roles.get(RoleEnum.PROPRIETOR));
        
        Group managementGroup = Group.builder()
                .name("PHARMACY_MANAGEMENT")
                .description("Group for pharmacy management team")
                .roles(managementRoles)
                .build();
        groupRepository.save(managementGroup);
        
        // Create Admin Group
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(roles.get(RoleEnum.ADMIN));
        adminRoles.add(roles.get(RoleEnum.SUPER_ADMIN));
        
        Group adminGroup = Group.builder()
                .name("SYSTEM_ADMINISTRATORS")
                .description("Group for system administrators")
                .roles(adminRoles)
                .build();
        groupRepository.save(adminGroup);
    }

    private Permission createPermission(String name, String description, 
                                       ResourceType resourceType, OperationType operationType, 
                                       boolean requiresApproval) {
        log.info("Creating permission: {}", name);
        Permission permission = Permission.builder()
                .name(name)
                .description(description)
                .resourceType(resourceType)
                .operationType(operationType)
                .requiresApproval(requiresApproval)
                .build();
        return permissionRepository.save(permission);
    }
}