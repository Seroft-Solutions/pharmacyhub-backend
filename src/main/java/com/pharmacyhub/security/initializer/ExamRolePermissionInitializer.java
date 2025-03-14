
package com.pharmacyhub.security.initializer;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.constants.ExamPermissionConstants;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import com.pharmacyhub.security.service.PermissionDataLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Initializes exam-specific roles and permissions
 * Runs after the main role initializer to ensure all base roles are already created
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 10) // Ensure this runs after the main role initializer
public class ExamRolePermissionInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final RolesRepository rolesRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionDataLoaderService permissionDataLoaderService;
    
    private boolean initialized = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Only initialize once
        if (initialized) {
            return;
        }
        
        try {
            log.info("Initializing exam-specific roles and permissions");
            
            // Ensure permissions are synchronized first
            permissionDataLoaderService.synchronizePermissions();
            
            // Assign exam permissions to roles
            assignExamPermissionsToRoles();
            
            // Create specific exam roles if needed
            createSpecificExamRoles();
            
            initialized = true;
            log.info("Exam role and permission initialization completed successfully");
        } catch (Exception e) {
            log.error("Error initializing exam roles and permissions: ", e);
        }
    }

    /**
     * Assign exam permissions to the appropriate roles
     */
    private void assignExamPermissionsToRoles() {
        log.info("Assigning exam permissions to roles");
        
        // Get all roles
        Optional<Role> adminRole = rolesRepository.findByName(RoleEnum.ADMIN);
        Optional<Role> superAdminRole = rolesRepository.findByName(RoleEnum.SUPER_ADMIN);
        Optional<Role> instructorRole = rolesRepository.findByName(RoleEnum.INSTRUCTOR);
        Optional<Role> studentRole = rolesRepository.findByName(RoleEnum.STUDENT);
        
        // Get existing exam permissions
        List<Permission> examPermissions = permissionDataLoaderService.getExamPermissions();
        Map<String, Permission> permissionMap = examPermissions.stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));
        
        // Assign admin permissions
        if (adminRole.isPresent()) {
            assignPermissionsToRole(adminRole.get(), 
                Arrays.asList(ExamPermissionConstants.ADMIN_PERMISSIONS), 
                permissionMap);
        }
        
        // Super admin gets all permissions
        if (superAdminRole.isPresent()) {
            assignPermissionsToRole(superAdminRole.get(), 
                Arrays.asList(ExamPermissionConstants.ADMIN_PERMISSIONS), 
                permissionMap);
        }
        
        // Instructor permissions
        if (instructorRole.isPresent()) {
            assignPermissionsToRole(instructorRole.get(), 
                Arrays.asList(ExamPermissionConstants.INSTRUCTOR_PERMISSIONS), 
                permissionMap);
        }
        
        // Student permissions
        if (studentRole.isPresent()) {
            assignPermissionsToRole(studentRole.get(), 
                Arrays.asList(ExamPermissionConstants.STUDENT_PERMISSIONS), 
                permissionMap);
        }
    }
    
    /**
     * Assign permissions to a role
     */
    private void assignPermissionsToRole(Role role, List<String> permissionNames, 
                                        Map<String, Permission> permissionMap) {
        log.info("Assigning {} permissions to role: {}", permissionNames.size(), role.getName());
        
        // Get current permissions for the role
        Set<Permission> currentPermissions = role.getPermissions();
        if (currentPermissions == null) {
            currentPermissions = new HashSet<>();
            role.setPermissions(currentPermissions);
        }
        
        // Add each permission
        for (String permName : permissionNames) {
            Permission permission = permissionMap.get(permName);
            if (permission != null) {
                currentPermissions.add(permission);
            } else {
                log.warn("Permission not found: {}", permName);
            }
        }
        
        // Save the updated role
        rolesRepository.save(role);
    }
    
    /**
     * Create specific exam-related roles if they don't exist
     */
    private void createSpecificExamRoles() {
        log.info("Creating specific exam roles");
        
        // First check if some roles need to be created
        
        // Example: Create an EXAM_CREATOR role with specific permissions
        if (!rolesRepository.findByName(RoleEnum.EXAM_CREATOR).isPresent()) {
            log.info("Creating EXAM_CREATOR role");
            
            Role examCreatorRole = Role.builder()
                    .name(RoleEnum.EXAM_CREATOR)
                    .description("Role for creating and managing exams")
                    .precedence(70) // Between INSTRUCTOR and PHARMACIST
                    .system(true)
                    .build();
            
            // Save the role first to get an ID
            examCreatorRole = rolesRepository.save(examCreatorRole);
            
            // Get permissions for this role
            List<Permission> examPermissions = permissionDataLoaderService.getExamPermissions();
            Map<String, Permission> permissionMap = examPermissions.stream()
                    .collect(Collectors.toMap(Permission::getName, p -> p));
            
            // Assign specific permissions
            String[] permissionNames = {
                ExamPermissionConstants.CREATE_EXAM,
                ExamPermissionConstants.EDIT_EXAM,
                ExamPermissionConstants.MANAGE_QUESTIONS,
                ExamPermissionConstants.VIEW_EXAMS
            };
            
            Set<Permission> rolePermissions = new HashSet<>();
            for (String permName : permissionNames) {
                Permission permission = permissionMap.get(permName);
                if (permission != null) {
                    rolePermissions.add(permission);
                }
            }
            
            examCreatorRole.setPermissions(rolePermissions);
            rolesRepository.save(examCreatorRole);
        }
    }
}
