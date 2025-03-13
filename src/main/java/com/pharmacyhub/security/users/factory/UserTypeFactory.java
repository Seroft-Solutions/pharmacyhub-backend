package com.pharmacyhub.security.users.factory;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.infrastructure.RolesRepository;
import com.pharmacyhub.security.service.RBACService;
import com.pharmacyhub.constants.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory service for creating different types of users with appropriate
 * roles and permissions.
 */
@Service
@Slf4j
public class UserTypeFactory {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final RBACService rbacService;

    @Autowired
    public UserTypeFactory(
            UserRepository userRepository,
            GroupRepository groupRepository,
            RolesRepository rolesRepository,
            PasswordEncoder passwordEncoder,
            RBACService rbacService) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
        this.rbacService = rbacService;
    }

    /**
     * Creates a Super Admin user with full system access.
     * 
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param password User's password
     * @param extraInfo Additional user information (optional)
     * @return The created Super Admin user
     */
    @Transactional
    public User createSuperAdmin(String email, String firstName, String lastName, String password, String... extraInfo) {
        log.info("Creating Super Admin user: {}", email);
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmailAddress(email);
        if (existingUser.isPresent()) {
            log.warn("User with email {} already exists, returning existing user", email);
            return existingUser.get();
        }
        
        // Create base user with SUPER_ADMIN type
        User user = createBaseUser(email, firstName, lastName, password, UserType.SUPER_ADMIN);
        
        // Add SUPER_ADMIN role directly
        Optional<Role> superAdminRole = rolesRepository.findByName(RoleEnum.SUPER_ADMIN);
        superAdminRole.ifPresent(role -> user.setRole(role));
        
        // Save user to generate ID
        user = userRepository.save(user);
        
        // Assign to SuperAdmins group
        Group superAdminGroup = groupRepository.findByName("SuperAdmins")
                .orElseThrow(() -> new RuntimeException("SuperAdmins group not found"));
        user.getGroups().add(superAdminGroup);
        
        // Add contact number if provided
        if (extraInfo != null && extraInfo.length > 0 && extraInfo[0] != null) {
            user.setContactNumber(extraInfo[0]);
        }
        
        return userRepository.save(user);
    }

    /**
     * Creates a regular Admin user with administrative privileges.
     * 
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param password User's password
     * @param extraInfo Additional user information (optional)
     * @return The created Admin user
     */
    @Transactional
    public User createAdmin(String email, String firstName, String lastName, String password, String... extraInfo) {
        log.info("Creating Admin user: {}", email);
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmailAddress(email);
        if (existingUser.isPresent()) {
            log.warn("User with email {} already exists, returning existing user", email);
            return existingUser.get();
        }
        
        // Create user with ADMIN type
        User user = createBaseUser(email, firstName, lastName, password, UserType.ADMIN);
        
        // Add ADMIN role directly
        Optional<Role> adminRole = rolesRepository.findByName(RoleEnum.ADMIN);
        adminRole.ifPresent(role -> user.setRole(role));
        
        // Save user to generate ID
        user = userRepository.save(user);
        
        // Assign to Administrators group
        Group adminGroup = groupRepository.findByName("Administrators")
                .orElseThrow(() -> new RuntimeException("Administrators group not found"));
        user.getGroups().add(adminGroup);
        
        // Add contact number if provided
        if (extraInfo != null && extraInfo.length > 0 && extraInfo[0] != null) {
            user.setContactNumber(extraInfo[0]);
        }
        
        return userRepository.save(user);
    }

    /**
     * Creates a Demo user with limited access for demonstration purposes.
     * 
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param password User's password
     * @param extraInfo Additional user information (optional)
     * @return The created Demo user
     */
    @Transactional
    public User createDemoUser(String email, String firstName, String lastName, String password, String... extraInfo) {
        log.info("Creating Demo user: {}", email);
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmailAddress(email);
        if (existingUser.isPresent()) {
            log.warn("User with email {} already exists, returning existing user", email);
            return existingUser.get();
        }
        
        // Create user with USER type for demo
        User user = createBaseUser(email, firstName, lastName, password, UserType.USER);
        
        // Add USER role directly
        Optional<Role> userRole = rolesRepository.findByName(RoleEnum.USER);
        userRole.ifPresent(role -> user.setRole(role));
        
        // Save user to generate ID
        user = userRepository.save(user);
        
        // Assign to DemoUsers group
        Group demoGroup = groupRepository.findByName("DemoUsers")
                .orElseThrow(() -> new RuntimeException("DemoUsers group not found"));
        user.getGroups().add(demoGroup);
        
        // Add contact number if provided
        if (extraInfo != null && extraInfo.length > 0 && extraInfo[0] != null) {
            user.setContactNumber(extraInfo[0]);
        }
        
        return userRepository.save(user);
    }

    /**
     * Creates a base user with common properties.
     * 
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param password User's password
     * @param userType User's type
     * @return The created base user
     */
    private User createBaseUser(
            String email, 
            String firstName, 
            String lastName, 
            String password,
            UserType userType) {
        User user = new User();
        user.setEmailAddress(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserType(userType);
        user.setActive(true);
        user.setVerified(true);
        user.setRegistered(true);
        user.setAccountNonLocked(true);
        user.setOpenToConnect(true);
        user.setRoles(new HashSet<>());
        user.setGroups(new HashSet<>());
        return user;
    }
}