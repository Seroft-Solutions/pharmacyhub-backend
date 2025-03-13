package com.pharmacyhub.security.users.groups;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds predefined groups for different user types.
 * Creates three main groups:
 * - SuperAdmins: Full system access
 * - Administrators: Administrative access but not system settings
 * - DemoUsers: Limited read-only access for demonstration
 */
@Component
@Slf4j
public class GroupSeeder {
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private RolesRepository rolesRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * Initialize the predefined groups.
     */
    @PostConstruct
    @Transactional
    public void init() {
        if (groupRepository.count() == 0) {
            log.info("Initializing default user groups...");
            createSuperAdminGroup();
            createAdminGroup();
            createDemoUserGroup();
            log.info("Group initialization completed.");
        }
    }

    /**
     * Creates the SuperAdmins group with full system access.
     */
    private void createSuperAdminGroup() {
        if (groupRepository.findByName("SuperAdmins").isEmpty()) {
            log.info("Creating SuperAdmins group...");
            
            // Find the SUPER_ADMIN role
            Optional<Role> superAdminRole = rolesRepository.findByName(RoleEnum.SUPER_ADMIN);
            
            if (superAdminRole.isPresent()) {
                Set<Role> roles = new HashSet<>();
                roles.add(superAdminRole.get());
                
                // Also add the ADMIN role
                Optional<Role> adminRole = rolesRepository.findByName(RoleEnum.ADMIN);
                adminRole.ifPresent(roles::add);
                
                Group group = Group.builder()
                    .name("SuperAdmins")
                    .description("Super administrators with full system access")
                    .roles(roles)
                    .build();
                    
                groupRepository.save(group);
                log.info("SuperAdmins group created successfully");
            } else {
                log.error("Could not create SuperAdmins group - SUPER_ADMIN role not found");
            }
        }
    }

    /**
     * Creates the Administrators group with administrative privileges.
     */
    private void createAdminGroup() {
        if (groupRepository.findByName("Administrators").isEmpty()) {
            log.info("Creating Administrators group...");
            
            // Find the ADMIN role
            Optional<Role> adminRole = rolesRepository.findByName(RoleEnum.ADMIN);
            
            if (adminRole.isPresent()) {
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole.get());
                
                Group group = Group.builder()
                    .name("Administrators")
                    .description("Regular administrators with management privileges")
                    .roles(roles)
                    .build();
                    
                groupRepository.save(group);
                log.info("Administrators group created successfully");
            } else {
                log.error("Could not create Administrators group - ADMIN role not found");
            }
        }
    }

    /**
     * Creates the DemoUsers group with limited access for demonstration.
     */
    private void createDemoUserGroup() {
        if (groupRepository.findByName("DemoUsers").isEmpty()) {
            log.info("Creating DemoUsers group...");
            
            // Find the USER role
            Optional<Role> userRole = rolesRepository.findByName(RoleEnum.USER);
            
            if (userRole.isPresent()) {
                Set<Role> roles = new HashSet<>();
                roles.add(userRole.get());
                
                Group group = Group.builder()
                    .name("DemoUsers")
                    .description("Demo users with limited read-only access")
                    .roles(roles)
                    .build();
                    
                groupRepository.save(group);
                log.info("DemoUsers group created successfully");
            } else {
                log.error("Could not create DemoUsers group - USER role not found");
            }
        }
    }
}