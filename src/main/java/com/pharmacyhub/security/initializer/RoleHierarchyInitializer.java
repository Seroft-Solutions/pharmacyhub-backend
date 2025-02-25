package com.pharmacyhub.security.initializer;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Initializes role hierarchy relationships
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleHierarchyInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final RolesRepository rolesRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Wait for the RoleInitializer to complete
        if (rolesRepository.count() == 0) {
            log.info("Skipping role hierarchy initialization - roles not yet initialized");
            return;
        }
        
        if (isHierarchyInitialized()) {
            log.info("Role hierarchy already initialized");
            return;
        }

        log.info("Initializing role hierarchy");
        try {
            setupHierarchy();
            log.info("Role hierarchy initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing role hierarchy: ", e);
        }
    }
    
    private boolean isHierarchyInitialized() {
        // Check if super admin role has child roles
        Optional<Role> superAdminRole = rolesRepository.findByName(RoleEnum.SUPER_ADMIN);
        if (superAdminRole.isPresent()) {
            Role role = rolesRepository.findByIdWithChildRoles(superAdminRole.get().getId());
            return role != null && !role.getChildRoles().isEmpty();
        }
        return false;
    }

    private void setupHierarchy() {
        // Load all roles
        Map<RoleEnum, Role> roleMap = new HashMap<>();
        
        for (RoleEnum roleEnum : RoleEnum.values()) {
            Optional<Role> role = rolesRepository.findByName(roleEnum);
            role.ifPresent(r -> roleMap.put(roleEnum, r));
        }
        
        // Setup hierarchy (higher precedence roles contain lower precedence ones)
        // SUPER_ADMIN > ADMIN > PROPRIETOR > PHARMACY_MANAGER > PHARMACIST/SALESMAN > USER
        
        // SUPER_ADMIN contains ADMIN
        setupChildRole(roleMap, RoleEnum.SUPER_ADMIN, RoleEnum.ADMIN);
        
        // ADMIN contains PROPRIETOR
        setupChildRole(roleMap, RoleEnum.ADMIN, RoleEnum.PROPRIETOR);
        
        // PROPRIETOR contains PHARMACY_MANAGER
        setupChildRole(roleMap, RoleEnum.PROPRIETOR, RoleEnum.PHARMACY_MANAGER);
        
        // PHARMACY_MANAGER contains PHARMACIST and SALESMAN
        setupChildRole(roleMap, RoleEnum.PHARMACY_MANAGER, RoleEnum.PHARMACIST);
        setupChildRole(roleMap, RoleEnum.PHARMACY_MANAGER, RoleEnum.SALESMAN);
        
        // PHARMACIST and SALESMAN contain USER
        setupChildRole(roleMap, RoleEnum.PHARMACIST, RoleEnum.USER);
        setupChildRole(roleMap, RoleEnum.SALESMAN, RoleEnum.USER);
    }
    
    private void setupChildRole(Map<RoleEnum, Role> roleMap, RoleEnum parentEnum, RoleEnum childEnum) {
        if (!roleMap.containsKey(parentEnum) || !roleMap.containsKey(childEnum)) {
            log.warn("Cannot set up hierarchy for {} > {}: one or both roles missing", parentEnum, childEnum);
            return;
        }
        
        Role parentRole = roleMap.get(parentEnum);
        Role childRole = roleMap.get(childEnum);
        
        // Load parent with child roles
        Role parent = rolesRepository.findByIdWithChildRoles(parentRole.getId());
        if (parent == null) {
            log.warn("Parent role {} not found with ID {}", parentEnum, parentRole.getId());
            return;
        }
        
        // Add child role if not already present
        if (parent.getChildRoles().stream().noneMatch(r -> r.getId().equals(childRole.getId()))) {
            parent.getChildRoles().add(childRole);
            rolesRepository.save(parent);
            log.info("Added {} as child of {}", childEnum, parentEnum);
        }
    }
}