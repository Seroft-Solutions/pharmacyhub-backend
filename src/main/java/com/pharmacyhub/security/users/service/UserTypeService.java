package com.pharmacyhub.security.users.service;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Group;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.infrastructure.GroupRepository;
import com.pharmacyhub.security.service.RBACService;
import com.pharmacyhub.security.users.factory.UserTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing users of different types (SuperAdmin, Admin, DemoUser).
 * Provides user type-specific operations and queries.
 */
@Service
@Slf4j
public class UserTypeService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final RBACService rbacService;
    private final UserTypeFactory userTypeFactory;
    
    @Autowired
    public UserTypeService(
            UserRepository userRepository,
            GroupRepository groupRepository,
            RBACService rbacService,
            UserTypeFactory userTypeFactory) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.rbacService = rbacService;
        this.userTypeFactory = userTypeFactory;
    }
    
    /**
     * Gets all super admin users.
     * Requires SUPER_ADMIN or ADMIN role.
     * 
     * @return List of super admin users
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public List<User> getAllSuperAdmins() {
        Group superAdminGroup = groupRepository.findByName("SuperAdmins")
                .orElseThrow(() -> new RuntimeException("SuperAdmins group not found"));
        return userRepository.findByGroupsContaining(superAdminGroup);
    }
    
    /**
     * Gets all admin users.
     * Requires SUPER_ADMIN or ADMIN role.
     * 
     * @return List of admin users
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public List<User> getAllAdmins() {
        Group adminGroup = groupRepository.findByName("Administrators")
                .orElseThrow(() -> new RuntimeException("Administrators group not found"));
        return userRepository.findByGroupsContaining(adminGroup);
    }
    
    /**
     * Gets all demo users.
     * Requires SUPER_ADMIN or ADMIN role.
     * 
     * @return List of demo users
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public List<User> getAllDemoUsers() {
        Group demoGroup = groupRepository.findByName("DemoUsers")
                .orElseThrow(() -> new RuntimeException("DemoUsers group not found"));
        return userRepository.findByGroupsContaining(demoGroup);
    }
    
    /**
     * Gets all users of a specific type.
     * Requires SUPER_ADMIN or ADMIN role.
     * 
     * @param userType The user type to filter by
     * @param pageable Pagination information
     * @return Page of users of the specified type
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public Page<User> getUsersByType(UserType userType, Pageable pageable) {
        return userRepository.findByUserType(userType, pageable);
    }
    
    /**
     * Creates a new super admin user.
     * Requires SUPER_ADMIN role.
     * 
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param password User's password
     * @param contactNumber User's contact number (optional)
     * @return The created super admin user
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public User createSuperAdmin(String email, String firstName, String lastName, String password, String contactNumber) {
        return userTypeFactory.createSuperAdmin(email, firstName, lastName, password, contactNumber);
    }
    
    /**
     * Creates a new admin user.
     * Requires SUPER_ADMIN role.
     * 
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param password User's password
     * @param contactNumber User's contact number (optional)
     * @return The created admin user
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public User createAdmin(String email, String firstName, String lastName, String password, String contactNumber) {
        return userTypeFactory.createAdmin(email, firstName, lastName, password, contactNumber);
    }
    
    /**
     * Creates a new demo user.
     * Requires SUPER_ADMIN or ADMIN role.
     * 
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param password User's password
     * @param contactNumber User's contact number (optional)
     * @return The created demo user
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Transactional
    public User createDemoUser(String email, String firstName, String lastName, String password, String contactNumber) {
        return userTypeFactory.createDemoUser(email, firstName, lastName, password, contactNumber);
    }
    
    /**
     * Gets the permissions for a specific user.
     * Requires SUPER_ADMIN or ADMIN role, or the user to be accessing their own permissions.
     * 
     * @param userId The user ID
     * @return Set of permissions for the user
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or authentication.principal.id == #userId")
    public Set<Permission> getUserPermissions(Long userId) {
        return rbacService.getUserEffectivePermissions(userId);
    }
    
    /**
     * Gets the roles for a specific user.
     * Requires SUPER_ADMIN or ADMIN role, or the user to be accessing their own roles.
     * 
     * @param userId The user ID
     * @return Set of roles for the user
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or authentication.principal.id == #userId")
    public Set<Role> getUserRoles(Long userId) {
        return rbacService.getUserRoles(userId);
    }
    
    /**
     * Checks if a user is a super admin.
     * 
     * @param userId The user ID
     * @return true if the user is a super admin, false otherwise
     */
    public boolean isSuperAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        // Check if user has SUPER_ADMIN role or is in SuperAdmins group
        return user.getUserType() == UserType.SUPER_ADMIN || 
               user.getGroups().stream().anyMatch(group -> "SuperAdmins".equals(group.getName()));
    }
    
    /**
     * Checks if a user is an admin.
     * 
     * @param userId The user ID
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        // Check if user has ADMIN role or is in Administrators group
        return user.getUserType() == UserType.ADMIN || 
               user.getGroups().stream().anyMatch(group -> "Administrators".equals(group.getName()));
    }
    
    /**
     * Checks if a user is a demo user.
     * 
     * @param userId The user ID
     * @return true if the user is a demo user, false otherwise
     */
    public boolean isDemoUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        // Check if user is in DemoUsers group
        return user.getGroups().stream().anyMatch(group -> "DemoUsers".equals(group.getName()));
    }
}