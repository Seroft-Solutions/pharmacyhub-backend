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

    private static final Map<RoleEnum, Integer> ROLE_PRECEDENCE = Map.ofEntries(
        Map.entry(RoleEnum.SUPER_ADMIN, 1),
        Map.entry(RoleEnum.ADMIN, 2),
        Map.entry(RoleEnum.PHARMACY_MANAGER, 3),
        Map.entry(RoleEnum.PROPRIETOR, 3),
        Map.entry(RoleEnum.PHARMACIST, 4),
        Map.entry(RoleEnum.INSTRUCTOR, 4),
        Map.entry(RoleEnum.SALESMAN, 5),
        Map.entry(RoleEnum.STUDENT, 6),
        Map.entry(RoleEnum.EXAM_CREATOR, 5),
        Map.entry(RoleEnum.USER, 7)
    );

    private static final Map<RoleEnum, String> ROLE_DESCRIPTIONS = Map.ofEntries(
        Map.entry(RoleEnum.SUPER_ADMIN, "Super administrator with full system access"),
        Map.entry(RoleEnum.ADMIN, "Administrator with system management capabilities"),
        Map.entry(RoleEnum.PHARMACIST, "Licensed pharmacist user"),
        Map.entry(RoleEnum.PHARMACY_MANAGER, "Pharmacy manager user"),
        Map.entry(RoleEnum.PROPRIETOR, "Pharmacy proprietor/owner"),
        Map.entry(RoleEnum.SALESMAN, "Sales representative"),
        Map.entry(RoleEnum.INSTRUCTOR, "Instructor who can create and manage exams"),
        Map.entry(RoleEnum.STUDENT, "Student who can take exams"),
        Map.entry(RoleEnum.EXAM_CREATOR, "User who can create and manage exams"),
        Map.entry(RoleEnum.USER, "Basic system user")
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
