package com.pharmacyhub.security.service;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final RolesRepository rolesRepository;
    private final UserRepository userRepository;

    public List<Role> getSystemRoles() {
        return rolesRepository.findBySystemTrue();
    }

    public List<Role> getAssignableRoles(String roleName, Long userId) {
        Role role = rolesRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<Role> userRoles = user.getRoles().stream().toList();

        return rolesRepository.findByPrecedenceLessThanEqual(role.getPrecedence())
                .stream()
                .filter(r -> !userRoles.contains(r))
                .collect(Collectors.toList());
    }
}
