package com.pharmacyhub.security.initializer;

import java.util.List;

import com.pharmacyhub.security.service.RoleHierarchyService;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Component
public class RoleHierarchyInitializer implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleHierarchyService roleHierarchyService;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initializeRoleHierarchy();
    }

    private void initializeRoleHierarchy() {
        List<Role> roles = roleRepository.findAll()
                .stream()
                .sorted((r1, r2) -> Integer.compare(r1.getPrecedence(), r2.getPrecedence()))
                .collect(Collectors.toList());

        // Clear existing hierarchies
        roles.forEach(role -> role.getChildRoles().clear());
        roleRepository.saveAll(roles);

        // Establish hierarchies based on precedence
        for (int i = 0; i < roles.size() - 1; i++) {
            Role parentRole = roles.get(i);
            Role childRole = roles.get(i + 1);

            if (parentRole.getPrecedence() < childRole.getPrecedence()) {
                try {
                    roleHierarchyService.addChildRole(parentRole.getId(), childRole.getId());
                } catch (Exception e) {
                    // Log error and continue with next pair
                    e.printStackTrace();
                }
            }
        }

        // Special handling for equal precedence roles
        for (int i = 0; i < roles.size(); i++) {
            for (int j = i + 1; j < roles.size(); j++) {
                Role role1 = roles.get(i);
                Role role2 = roles.get(j);

                if (role1.getPrecedence() == role2.getPrecedence()) {
                    // Find the parent role (role with next higher precedence)
                    List<Role> parentRoles = roles.stream()
                            .filter(r -> r.getPrecedence() < role1.getPrecedence())
                            .sorted((r1, r2) -> Integer.compare(r1.getPrecedence(), r2.getPrecedence()))
                            .collect(Collectors.toList());
                    if (!parentRoles.isEmpty()) {
                        Role parentRole = parentRoles.get(parentRoles.size() - 1);
                        try {
                            roleHierarchyService.addChildRole(parentRole.getId(), role1.getId());
                            roleHierarchyService.addChildRole(parentRole.getId(), role2.getId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
