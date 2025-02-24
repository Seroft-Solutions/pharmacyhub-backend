package com.pharmacyhub.service;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.dto.PHUserDTO;
import java.util.Optional;
import com.pharmacyhub.entity.enums.UserType;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.pharmacyhub.dto.ChangePasswordDTO;

@Service
@RequiredArgsConstructor
public class UserService {
    private final RolesRepository rolesRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public PHUserDTO saveUser(UserDTO userDTO) {
        // Implementation for saving user
        return null; // Replace with actual implementation
    }

    public boolean verifyUser(String token) {
        // Implementation for verifying user
        return false; // Replace with actual implementation
    }

    public List<User> getUsers() {
        // Implementation for getting all users
        return userRepository.findAll(); // Replace with actual implementation
    }

     public UserType getUserType(Long userId) {
         User user = userRepository.findById(userId)
                 .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return user.getUserType();
    }

    public User getUserByEmailAddress(UserDTO userDTO) {
        Optional<User> userOptional = userRepository.findByEmailAddress(userDTO.getEmailAddress());
        return userOptional.orElse(null);
    }

    public boolean forgotPassword(UserDTO userDTO) {
         Optional<User> userOptional = userRepository.findByEmailAddress(userDTO.getEmailAddress());
         if (userOptional.isPresent()) {
             User user = userOptional.get();
             user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
             userRepository.save(user);
             return true;
         }
         return false;
    }

    public PHUserDTO changeUserPassword(ChangePasswordDTO changePasswordDTO) {
        // Implementation for changing user password
        return null;
    }

    public PHUserDTO editUserInformation(UserDTO userDTO) {
        // Implementation for editing user information
        return null;
    }

    public List<User> findAll() {
        // Implementation for finding all users
        return userRepository.findAll();
    }

    public PHUserDTO getUserCompleteInformation() {
        // Implementation for getting complete user information
        return null;
    }

    public boolean updateUserStatus() {
        // Implementation for updating user status
        return false;
    }

    public User getLoggedInUser() {
        // Implementation for getting logged in user
        return null;
    }
}
