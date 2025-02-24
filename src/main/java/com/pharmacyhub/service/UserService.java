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
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmailAddress(userDTO.getEmailAddress());
        if (existingUser.isPresent()) {
            return null;
        }

        // Create new user
        User user = new User();
        user.setEmailAddress(userDTO.getEmailAddress());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setVerificationToken(java.util.UUID.randomUUID().toString());
        user.setVerified(false);
        user.setActive(true);
        user.setRegistered(true);
        user.setAccountNonLocked(true);
        user.setOpenToConnect(true);

        // Save user
        user = userRepository.save(user);

        // Convert to DTO and return
        return UserDTO.builder()
            .id(user.getId())
            .emailAddress(user.getEmailAddress())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .openToConnect(user.isOpenToConnect())
            .registered(user.isRegistered())
            .build();
    }

    public boolean verifyUser(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
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
