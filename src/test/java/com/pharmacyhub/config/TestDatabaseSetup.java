package com.pharmacyhub.config;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.PermissionRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class to handle role creation in tests to avoid duplicate roles.
 * This class will ensure that a role is only created once for test purposes.
 */
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestDatabaseSetup {

    private final RolesRepository rolesRepository;
    private final PermissionRepository permissionRepository;
    
    // Cache for created roles to avoid duplicates
    private static final Map<RoleEnum, Role> roleCache = new ConcurrentHashMap<>();

    /**
     * Get or create a role with the specified name
     * 
     * @param roleEnum the role enum to get or create
     * @param precedence the precedence of the role
     * @return the role instance
     */
    @Transactional
    public Role getOrCreateRole(RoleEnum roleEnum, int precedence) {
        // First check the cache
        if (roleCache.containsKey(roleEnum)) {
            return roleCache.get(roleEnum);
        }
        
        // Check if the role already exists in the database
        Optional<Role> existingRole = rolesRepository.findByName(roleEnum);
        if (existingRole.isPresent()) {
            Role role = existingRole.get();
            roleCache.put(roleEnum, role);
            return role;
        }
        
        // Create a new role if it doesn't exist
        Role newRole = Role.builder()
                .name(roleEnum)
                .description(roleEnum.name() + " role")
                .precedence(precedence)
                .system(true)
                .permissions(new HashSet<>())
                .childRoles(new HashSet<>())
                .build();
                
        Role savedRole = rolesRepository.save(newRole);
        roleCache.put(roleEnum, savedRole);
        return savedRole;
    }
    
    /**
     * Clear all roles from the database and cache
     */
    @Transactional
    public void clearAllRoles() {
        roleCache.clear();
        rolesRepository.deleteAll();
    }
    
    /**
     * Initialize all standard roles for testing
     */
    @Transactional
    public void initializeStandardRoles() {
        clearAllRoles();
        getOrCreateRole(RoleEnum.USER, 100);
        getOrCreateRole(RoleEnum.ADMIN, 20);
        getOrCreateRole(RoleEnum.PHARMACIST, 80);
        getOrCreateRole(RoleEnum.PHARMACY_MANAGER, 60);
        getOrCreateRole(RoleEnum.PROPRIETOR, 40);
        getOrCreateRole(RoleEnum.SALESMAN, 90);
        getOrCreateRole(RoleEnum.SUPER_ADMIN, 10);
    }
}
