package com.pharmacyhub.seeder;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RoleSeeder {
    @Autowired
    private RoleRepository roleRepository;

    private static final Map<RoleEnum, Integer> ROLE_PRECEDENCE = Map.of(
        RoleEnum.SUPER_ADMIN, 1,
        RoleEnum.ADMIN, 2,
        RoleEnum.PHARMACIST, 3,
        RoleEnum.PHARMACY_MANAGER, 3,
        RoleEnum.PROPRIETOR, 3,
        RoleEnum.SALESMAN, 4,
        RoleEnum.USER, 5
    );

    private static final Map<RoleEnum, String> ROLE_DESCRIPTIONS = Map.of(
        RoleEnum.SUPER_ADMIN, "Super administrator with full system access",
        RoleEnum.ADMIN, "Administrator with system management capabilities",
        RoleEnum.PHARMACIST, "Licensed pharmacist user",
        RoleEnum.PHARMACY_MANAGER, "Pharmacy manager user",
        RoleEnum.PROPRIETOR, "Pharmacy proprietor/owner",
        RoleEnum.SALESMAN, "Sales representative",
        RoleEnum.USER, "Basic system user"
    );

    public void loadRoles() {
        ROLE_PRECEDENCE.forEach((roleName, precedence) -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder()
                    .name(roleName)
                    .description(ROLE_DESCRIPTIONS.get(roleName))
                    .precedence(precedence)
                    .system(true)
                    .build();
                roleRepository.save(role);
            }
        });
    }
}
