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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import com.pharmacyhub.entity.enums.UserType;
import org.springframework.security.core.Authentication;
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
            .contactNumber(user.getContactNumber())
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
        User currentUser = getLoggedInUser();
        if (currentUser != null && passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), currentUser.getPassword())) {
            currentUser.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
            userRepository.save(currentUser);
            return UserDTO.builder()
                .id(currentUser.getId())
                .emailAddress(currentUser.getEmailAddress())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .contactNumber(currentUser.getContactNumber())
                .openToConnect(currentUser.isOpenToConnect())
                .registered(currentUser.isRegistered())
                .build();
        }
        return null; 
    }

    public PHUserDTO editUserInformation(UserDTO userDTO) {
        User currentUser = getLoggedInUser();
        if (currentUser != null) {
            currentUser.setFirstName(userDTO.getFirstName());
            currentUser.setLastName(userDTO.getLastName());
            // Only update if provided
            if (userDTO.getContactNumber() != null) {
                currentUser.setContactNumber(userDTO.getContactNumber());
            }
            userRepository.save(currentUser);
            return UserDTO.builder()
                .id(currentUser.getId())
                .emailAddress(currentUser.getEmailAddress())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .contactNumber(currentUser.getContactNumber())
                .openToConnect(currentUser.isOpenToConnect())
                .registered(currentUser.isRegistered())
                .build();
        }
        return null; 
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public PHUserDTO getUserCompleteInformation() {
        User currentUser = getLoggedInUser();
        if (currentUser != null) {
            return UserDTO.builder()
                .id(currentUser.getId())
                .emailAddress(currentUser.getEmailAddress())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .contactNumber(currentUser.getContactNumber())
                .openToConnect(currentUser.isOpenToConnect())
                .registered(currentUser.isRegistered())
                .build();
        }
        return null;
 
    }

    public boolean updateUserStatus() {
        User currentUser = getLoggedInUser();
        if (currentUser != null) {
            currentUser.setOpenToConnect(!currentUser.isOpenToConnect());
            userRepository.save(currentUser);
            return true;
        }
        return false;
    }

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        String email = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : 
                      (principal instanceof User) ? ((User) principal).getEmailAddress() : principal.toString();
        Optional<User> userOptional = userRepository.findByEmailAddress(email);
 
        return userOptional.orElse(null);
    }
}
