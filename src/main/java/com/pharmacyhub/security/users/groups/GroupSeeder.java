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
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds predefined groups for different user types.
 * Creates three main groups:
 * - SuperAdmins: Full system access
 * - Administrators: Administrative access but not system settings
 * - DemoUsers: Limited read-only access for demonstration
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@Slf4j
public class GroupSeeder implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private RolesRepository rolesRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * Initialize the predefined groups.
     */
    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Checking for groups in onApplicationEvent...");
        
        // We don't need to create the groups anymore since RoleInitializer handles that,
        // but we'll verify they exist for DefaultUsersInitializer
        boolean adminGroupExists = groupRepository.findByName("Administrators").isPresent();
        boolean superAdminGroupExists = groupRepository.findByName("SuperAdmins").isPresent();
        boolean demoGroupExists = groupRepository.findByName("DemoUsers").isPresent();
        
        if (adminGroupExists && superAdminGroupExists && demoGroupExists) {
            log.info("All required groups exist. Ready for DefaultUsersInitializer.");
        } else {
            log.warn("Some required groups are missing. RoleInitializer may not have run correctly.");
            if (!adminGroupExists) log.warn("Missing: Administrators group");
            if (!superAdminGroupExists) log.warn("Missing: SuperAdmins group");
            if (!demoGroupExists) log.warn("Missing: DemoUsers group");
        }
    }
    
    /**
     * This method is kept for reference but not used directly anymore.
     * The initialization has been moved to onApplicationEvent.
     */
    @PostConstruct
    public void init() {
        log.info("PostConstruct method in GroupSeeder - not doing initialization here.");
    }

    /**
     * Creates the SuperAdmins group with full system access.
     * NOT directly called via PostConstruct - handled by RoleInitializer.
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
     * NOT directly called via PostConstruct - handled by RoleInitializer.
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
     * NOT directly called via PostConstruct - handled by RoleInitializer.
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