package com.pharmacyhub.security.initializer;

import com.pharmacyhub.security.constants.AuthPermissionConstants;
import com.pharmacyhub.security.constants.ExamPermissionConstants;
import com.pharmacyhub.security.domain.*;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import com.pharmacyhub.constants.RoleEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component("roleInitializer")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RoleInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final RolesRepository rolesRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;
    
    // Store the current working set of permissions
    private Map<String, Permission> permissionMap = new HashMap<>();

    @Override
    @Transactional(noRollbackFor = {Exception.class})
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            if (rolesRepository.count() > 0) {
                log.info("Roles already initialized");
                return;
            }

            log.info("Initializing default roles and permissions");
            // First get all existing permissions before attempting to create any new ones
            List<Permission> existingPermissions = permissionRepository.findAll();
            permissionMap = existingPermissions.stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));
            
            // Then initialize any missing permissions
            Map<String, Permission> permissions = initializePermissions(permissionMap);
            Map<RoleEnum, Role> roles = initializeRoles(permissions);
            initializeGroups(roles);
            
            log.info("Role initialization completed successfully");
        } catch (Exception e) {
            log.error("Error initializing roles: {}", e.getMessage());
            // Log the full stack trace at debug level
            log.debug("Full stack trace:", e);
            // Do not rethrow to prevent application startup from failing
        }
    }

    private Map<String, Permission> initializePermissions(Map<String, Permission> existingPermissions) {
        log.info("Initializing permissions...");
        Map<String, Permission> permissionMap = new HashMap<>(existingPermissions);
        
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
        
        // Auth permissions
        for (String permName : AuthPermissionConstants.BASIC_USER_PERMISSIONS) {
            permissionMap.put(permName, createPermission(permName,
                "Permission to " + permName.replace("auth:", "").replace("-", " "),
                ResourceType.USER, getOperationTypeForPermission(permName), false));
        }
        
        for (String permName : AuthPermissionConstants.ADMIN_PERMISSIONS) {
            permissionMap.put(permName, createPermission(permName,
                "Admin permission to " + permName.replace("auth:", "").replace("-", " "),
                ResourceType.USER, getOperationTypeForPermission(permName), false));
        }
        
        // Exam permissions for students
        for (String permName : ExamPermissionConstants.STUDENT_PERMISSIONS) {
            permissionMap.put(permName, createPermission(permName,
                "Student permission to " + permName.replace("exams:", "").replace("-", " "),
                ResourceType.PHARMACY, getOperationTypeForPermission(permName), false));
        }
        
        // Exam permissions for instructors
        for (String permName : ExamPermissionConstants.INSTRUCTOR_PERMISSIONS) {
            if (!permissionMap.containsKey(permName)) {
                permissionMap.put(permName, createPermission(permName,
                    "Instructor permission to " + permName.replace("exams:", "").replace("-", " "),
                    ResourceType.PHARMACY, getOperationTypeForPermission(permName), false));
            }
        }
        
        // Admin exam permissions
        for (String permName : ExamPermissionConstants.ADMIN_PERMISSIONS) {
            if (!permissionMap.containsKey(permName)) {
                permissionMap.put(permName, createPermission(permName,
                    "Admin permission to " + permName.replace("exams:", "").replace("-", " "),
                    ResourceType.PHARMACY, getOperationTypeForPermission(permName), false));
            }
        }
        
        return permissionMap;
    }
    
    /**
     * Helper method to determine operation type from permission name
     */
    private OperationType getOperationTypeForPermission(String permissionName) {
        String lowerName = permissionName.toLowerCase();
        if (lowerName.contains("create") || lowerName.contains("add")) {
            return OperationType.CREATE;
        } else if (lowerName.contains("view") || lowerName.contains("read") || lowerName.contains("login") || lowerName.contains("logout")) {
            return OperationType.READ;
        } else if (lowerName.contains("edit") || lowerName.contains("update") || lowerName.contains("reset") || lowerName.contains("verify")) {
            return OperationType.UPDATE;
        } else if (lowerName.contains("delete") || lowerName.contains("remove")) {
            return OperationType.DELETE;
        } else if (lowerName.contains("approve")) {
            return OperationType.APPROVE;
        } else if (lowerName.contains("reject")) {
            return OperationType.REJECT;
        } else if (lowerName.contains("manage") || lowerName.contains("impersonate")) {
            return OperationType.MANAGE;
        } else if (lowerName.contains("export")) {
            return OperationType.EXPORT;
        } else if (lowerName.contains("import")) {
            return OperationType.IMPORT;
        } else {
            return OperationType.READ; // Default to READ
        }
    }

    private Map<RoleEnum, Role> initializeRoles(Map<String, Permission> permissions) {
        log.info("Initializing roles...");
        Map<RoleEnum, Role> roleMap = new HashMap<>();

        // Create USER role (lowest precedence - 100)
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(permissions.get("VIEW_PROFILE"));
        userPermissions.add(permissions.get("UPDATE_PROFILE"));
        
        // Add auth basic user permissions
        for (String permName : AuthPermissionConstants.BASIC_USER_PERMISSIONS) {
            addPermissionIfExists(userPermissions, permissions, permName);
        }
        
        Role userRole = Role.builder()
                .name(RoleEnum.USER)
                .description("Base user role with minimal permissions")
                .precedence(100)
                .permissions(userPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.USER, rolesRepository.save(userRole));

        Role userRole2 = Role.builder()
                            .name(RoleEnum.ROLE_USER)
                            .description("Base user role with minimal permissions")
                            .precedence(100)
                            .permissions(userPermissions)
                            .system(true)
                            .build();
        roleMap.put(RoleEnum.ROLE_USER, rolesRepository.save(userRole2));
        
        // Create STUDENT role (precedence - 90)
        Set<Permission> studentPermissions = new HashSet<>(userPermissions);
        // Add student exam permissions
        for (String permName : ExamPermissionConstants.STUDENT_PERMISSIONS) {
            addPermissionIfExists(studentPermissions, permissions, permName);
        }
        
        Role studentRole = Role.builder()
                .name(RoleEnum.STUDENT)
                .description("Student role with exam taking permissions")
                .precedence(90)
                .permissions(studentPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.STUDENT, rolesRepository.save(studentRole));

        // Create PHARMACIST role (precedence - 80)
        Set<Permission> pharmacistPermissions = new HashSet<>(userPermissions);
        pharmacistPermissions.add(permissions.get("VIEW_PHARMACY_INVENTORY"));
        // Add exam permissions like view and take
        addPermissionIfExists(pharmacistPermissions, permissions, ExamPermissionConstants.VIEW_EXAMS);
        addPermissionIfExists(pharmacistPermissions, permissions, ExamPermissionConstants.TAKE_EXAM);
        
        Role pharmacistRole = Role.builder()
                .name(RoleEnum.PHARMACIST)
                .description("Pharmacist role with permissions to view inventory")
                .precedence(80)
                .permissions(pharmacistPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.PHARMACIST, rolesRepository.save(pharmacistRole));
        
        // Create TECHNICIAN role (precedence - 75)
        Set<Permission> technicianPermissions = new HashSet<>(pharmacistPermissions);
        // Additional technician permissions if needed
        

        // Create INSTRUCTOR role (precedence - 70)
        Set<Permission> instructorPermissions = new HashSet<>(userPermissions);
        // Add instructor exam permissions
        for (String permName : ExamPermissionConstants.INSTRUCTOR_PERMISSIONS) {
            addPermissionIfExists(instructorPermissions, permissions, permName);
        }
        
        Role instructorRole = Role.builder()
                .name(RoleEnum.INSTRUCTOR)
                .description("Instructor role with exam management permissions")
                .precedence(70)
                .permissions(instructorPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.INSTRUCTOR, rolesRepository.save(instructorRole));
        
        // Create EXAM_CREATOR role (precedence - 65)
        Set<Permission> examCreatorPermissions = new HashSet<>(userPermissions);
        // Add specific exam creation permissions
        addPermissionIfExists(examCreatorPermissions, permissions, ExamPermissionConstants.CREATE_EXAM);
        addPermissionIfExists(examCreatorPermissions, permissions, ExamPermissionConstants.EDIT_EXAM);
        addPermissionIfExists(examCreatorPermissions, permissions, ExamPermissionConstants.MANAGE_QUESTIONS);
        addPermissionIfExists(examCreatorPermissions, permissions, ExamPermissionConstants.VIEW_EXAMS);
        
        Role examCreatorRole = Role.builder()
                .name(RoleEnum.EXAM_CREATOR)
                .description("Role for creating and managing exams")
                .precedence(65)
                .permissions(examCreatorPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.EXAM_CREATOR, rolesRepository.save(examCreatorRole));

        // Create SALESMAN role (precedence - 85)
        Set<Permission> salesmanPermissions = new HashSet<>(userPermissions);
        salesmanPermissions.add(permissions.get("PROCESS_SALES"));
        salesmanPermissions.add(permissions.get("VIEW_SALES"));
        
        Role salesmanRole = Role.builder()
                .name(RoleEnum.SALESMAN)
                .description("Salesman role for processing sales")
                .precedence(85)
                .permissions(salesmanPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.SALESMAN, rolesRepository.save(salesmanRole));

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

        // Create ADMIN role (precedence - 20)
        // Admin gets ALL permissions just like super admin
        Set<Permission> adminPermissions = new HashSet<>(permissions.values());
        
        Role adminRole = Role.builder()
                .name(RoleEnum.ADMIN)
                .description("Administrator role with full system access")
                .precedence(20)
                .permissions(adminPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.ADMIN, rolesRepository.save(adminRole));

        // Create SUPER_ADMIN role (precedence - 10)
        // Super admin gets ALL permissions
        Set<Permission> superAdminPermissions = new HashSet<>(permissions.values());
        
        Role superAdminRole = Role.builder()
                .name(RoleEnum.SUPER_ADMIN)
                .description("Super administrator with highest privileges")
                .precedence(10)
                .permissions(superAdminPermissions)
                .system(true)
                .build();
        roleMap.put(RoleEnum.SUPER_ADMIN, rolesRepository.save(superAdminRole));

        return roleMap;
    }
    
    /**
     * Helper method to add a permission to a set if it exists in the map
     */
    private void addPermissionIfExists(Set<Permission> permissions, Map<String, Permission> permissionMap, String permissionName) {
        Permission permission = permissionMap.get(permissionName);
        if (permission != null) {
            permissions.add(permission);
        } else {
            log.warn("Permission not found: {}", permissionName);
        }
    }

    private void initializeGroups(Map<RoleEnum, Role> roles) {
        log.info("Initializing groups...");
        
        // Create Staff Group (Pharmacists, Salesmen, Technicians)
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
        
        // Create Education Group (Students, Instructors)
        Set<Role> educationRoles = new HashSet<>();
        educationRoles.add(roles.get(RoleEnum.STUDENT));
        educationRoles.add(roles.get(RoleEnum.INSTRUCTOR));
        educationRoles.add(roles.get(RoleEnum.EXAM_CREATOR));
        
        Group educationGroup = Group.builder()
                .name("EDUCATION")
                .description("Group for education-related roles")
                .roles(educationRoles)
                .build();
        groupRepository.save(educationGroup);
        
        // Create Admin Group
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(roles.get(RoleEnum.ADMIN));
        
        Group adminGroup = Group.builder()
                .name("Administrators")
                .description("Group for system administrators")
                .roles(adminRoles)
                .build();
        groupRepository.save(adminGroup);
        
        // Create SuperAdmin Group
        Set<Role> superAdminRoles = new HashSet<>();
        superAdminRoles.add(roles.get(RoleEnum.SUPER_ADMIN));
        superAdminRoles.add(roles.get(RoleEnum.ADMIN)); // Super admins also have admin roles
        
        Group superAdminGroup = Group.builder()
                .name("SuperAdmins")
                .description("Group for super administrators")
                .roles(superAdminRoles)
                .build();
        groupRepository.save(superAdminGroup);
        
        // Create Demo User Group
        Set<Role> demoRoles = new HashSet<>();
        demoRoles.add(roles.get(RoleEnum.USER));
        
        Group demoGroup = Group.builder()
                .name("DemoUsers")
                .description("Group for demo users")
                .roles(demoRoles)
                .build();
        groupRepository.save(demoGroup);
    }

    private Permission createPermission(String name, String description, 
                                       ResourceType resourceType, OperationType operationType, 
                                       boolean requiresApproval) {
        // First check if the permission is already in the permission map (from previous call)
        Permission permission = permissionMap.get(name);
        if (permission != null) {
            log.info("Permission already exists in map: {}", name);
            return permission;
        }

        log.info("Creating permission: {}", name);
        // Then check if permission already exists in the database to avoid duplicate key errors
        Optional<Permission> existingPermission = permissionRepository.findByName(name);
        if (existingPermission.isPresent()) {
            log.info("Permission already exists in database: {}", name);
            return existingPermission.get();
        }
        
        // If not found anywhere, create a new permission
        permission = Permission.builder()
                .name(name)
                .description(description)
                .resourceType(resourceType)
                .operationType(operationType)
                .requiresApproval(requiresApproval)
                .build();
        return permissionRepository.save(permission);
    }
}