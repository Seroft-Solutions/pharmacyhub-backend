package com.pharmacyhub.security.service;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.pharmacyhub.constants.RoleEnum;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final RolesRepository rolesRepository;
    private final UserRepository userRepository;

    public List<Role> getSystemRoles() {
        return rolesRepository.findBySystemTrue();
    }

    public List<Role> getAssignableRoles(String roleName, Long userId) {
        // Convert the roleName string to RoleEnum
        RoleEnum roleEnum = RoleEnum.fromString(roleName);
        
        Role role = rolesRepository.findByName(roleEnum)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<Role> userRoles = user.getRoles().stream().toList();

        return rolesRepository.findByPrecedenceLessThanEqual(role.getPrecedence())
                .stream()
                .filter(r -> !userRoles.contains(r))
                .collect(Collectors.toList());
    }
    
    /**
     * Assign a role to a user
     * 
     * @param userId The user ID
     * @param roleName The role name or enum string
     * @throws IllegalArgumentException if user or role not found
     */
    @Transactional
    public void assignRoleToUser(Long userId, String roleName) {
        // Convert the roleName string to RoleEnum
        RoleEnum roleEnum = RoleEnum.fromString(roleName);
        
        Role role = rolesRepository.findByName(roleEnum)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setRole(role);
        userRepository.save(user);
    }
}