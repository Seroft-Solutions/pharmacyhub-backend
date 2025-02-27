package com.pharmacyhub.controller;

import com.pharmacyhub.dto.UserProfileDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.service.RBACService;
import com.pharmacyhub.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private RBACService rbacService;
    
    /**
     * Get the complete profile of the currently authenticated user including roles and permissions
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        logger.debug("Received request for user profile");
        
        try {
            User currentUser = userService.getLoggedInUser();
            
            if (currentUser == null) {
                logger.warn("No authenticated user found");
                return ResponseEntity.notFound().build();
            }
            
            logger.debug("Found authenticated user: {}", currentUser.getEmailAddress());
        
            // Get user roles
            Set<Role> roles = rbacService.getUserRoles(currentUser.getId());
            Set<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
                
            logger.debug("User roles: {}", roleNames);
                
            // Get user permissions
            Set<Permission> permissions = rbacService.getUserEffectivePermissions(currentUser.getId());
            Set<String> permissionNames = permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
                
            logger.debug("User permissions count: {}", permissionNames.size());
        
            // Build the user profile response
            UserProfileDTO profile = UserProfileDTO.builder()
                .id(currentUser.getId())
                .emailAddress(currentUser.getEmailAddress())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .contactNumber(currentUser.getContactNumber())
                .userType(currentUser.getUserType())
                .registered(currentUser.isRegistered())
                .openToConnect(currentUser.isOpenToConnect())
                .verified(currentUser.isVerified())
                .roles(roleNames)
                .permissions(permissionNames)
                .build();
                
            logger.debug("Successfully built user profile response");
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error fetching user profile: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Health check endpoint to verify the controller is accessible
     */
    @GetMapping("/profile/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("User Profile API is operational");
    }
}
