package com.pharmacyhub.controller;

import com.pharmacyhub.constants.APIConstants;
import com.pharmacyhub.dto.UserProfileDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.service.RBACService;
import com.pharmacyhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for user profile operations
 * Provides endpoints for retrieving the complete user profile with roles and permissions
 */
@RestController
@RequestMapping(APIConstants.BASE_MAPPING)
public class UserProfileController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RBACService rbacService;
    
    /**
     * Get the complete profile of the currently authenticated user including roles and permissions
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(
            value = APIConstants.API_VERSION_V1 + "/users/profile",
            method = RequestMethod.GET
    )
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        User currentUser = userService.getLoggedInUser();
        
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(buildUserProfileDTO(currentUser));
    }
    
    /**
     * Get profile for a specific user (admin access required)
     */
    @PreAuthorize("hasRole('ADMIN') or hasPermission('USER', 'READ')")
    @RequestMapping(
            value = APIConstants.API_VERSION_V1 + "/users/{userId}/profile",
            method = RequestMethod.GET
    )
    public ResponseEntity<UserProfileDTO> getUserProfileById(@PathVariable Long userId) {
        User user = userService.findById(userId);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(buildUserProfileDTO(user));
    }
    
    /**
     * Refresh user permissions and roles
     * Useful after role/permission changes
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(
            value = APIConstants.API_VERSION_V1 + "/users/refresh-permissions",
            method = RequestMethod.POST
    )
    public ResponseEntity<UserProfileDTO> refreshUserPermissions() {
        User currentUser = userService.getLoggedInUser();
        
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(buildUserProfileDTO(currentUser));
    }
    
    /**
     * Helper method to build the user profile DTO
     */
    private UserProfileDTO buildUserProfileDTO(User user) {
        // Get user roles
        Set<Role> roles = rbacService.getUserRoles(user.getId());
        Set<String> roleNames = roles.stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
            
        // Get user permissions
        Set<Permission> permissions = rbacService.getUserEffectivePermissions(user.getId());
        Set<String> permissionNames = permissions.stream()
            .map(Permission::getName)
            .collect(Collectors.toSet());
        
        // Build the user profile response
        return UserProfileDTO.builder()
            .id(user.getId())
            .emailAddress(user.getEmailAddress())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .contactNumber(user.getContactNumber())
            .userType(user.getUserType())
            .registered(user.isRegistered())
            .openToConnect(user.isOpenToConnect())
            .verified(user.isVerified())
            .roles(roleNames)
            .permissions(permissionNames)
            .build();
    }
}