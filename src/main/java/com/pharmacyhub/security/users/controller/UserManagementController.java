package com.pharmacyhub.security.users.controller;

import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.security.domain.Permission;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.users.dto.UserCreationRequest;
import com.pharmacyhub.security.users.service.UserTypeService;
import com.pharmacyhub.security.users.util.UserConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for managing different user types.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {
    
    private final UserTypeService userTypeService;
    
    /**
     * Get all super admin users.
     * 
     * @return List of super admin users
     */
    @GetMapping("/super-admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PHUserDTO>> getAllSuperAdmins() {
        List<User> superAdmins = userTypeService.getAllSuperAdmins();
        List<PHUserDTO> superAdminDTOs = mapUsersToDTOs(superAdmins);
        return ResponseEntity.ok(superAdminDTOs);
    }
    
    /**
     * Get all admin users.
     * 
     * @return List of admin users
     */
    @GetMapping("/admins")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<PHUserDTO>> getAllAdmins() {
        List<User> admins = userTypeService.getAllAdmins();
        List<PHUserDTO> adminDTOs = mapUsersToDTOs(admins);
        return ResponseEntity.ok(adminDTOs);
    }
    
    /**
     * Get all demo users.
     * 
     * @return List of demo users
     */
    @GetMapping("/demo-users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<PHUserDTO>> getAllDemoUsers() {
        List<User> demoUsers = userTypeService.getAllDemoUsers();
        List<PHUserDTO> demoUserDTOs = mapUsersToDTOs(demoUsers);
        return ResponseEntity.ok(demoUserDTOs);
    }
    
    /**
     * Get users by type with pagination.
     * 
     * @param userType User type to filter by
     * @param pageable Pagination information
     * @return Page of users of the specified type
     */
    @GetMapping("/by-type/{userType}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Page<PHUserDTO>> getUsersByType(
            @PathVariable UserType userType,
            Pageable pageable) {
        Page<User> users = userTypeService.getUsersByType(userType, pageable);
        Page<PHUserDTO> userDTOs = users.map(this::mapUserToDTO);
        return ResponseEntity.ok(userDTOs);
    }
    
    /**
     * Create a new super admin user.
     * 
     * @param request User creation request
     * @return Created super admin user
     */
    @PostMapping("/super-admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PHUserDTO> createSuperAdmin(@Valid @RequestBody UserCreationRequest request) {
        User superAdmin = userTypeService.createSuperAdmin(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getContactNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapUserToDTO(superAdmin));
    }
    
    /**
     * Create a new admin user.
     * 
     * @param request User creation request
     * @return Created admin user
     */
    @PostMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PHUserDTO> createAdmin(@Valid @RequestBody UserCreationRequest request) {
        User admin = userTypeService.createAdmin(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getContactNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapUserToDTO(admin));
    }
    
    /**
     * Create a new demo user.
     * 
     * @param request User creation request
     * @return Created demo user
     */
    @PostMapping("/demo-users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<PHUserDTO> createDemoUser(@Valid @RequestBody UserCreationRequest request) {
        User demoUser = userTypeService.createDemoUser(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getContactNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapUserToDTO(demoUser));
    }
    
    /**
     * Get permissions for a user.
     * 
     * @param userId User ID
     * @return Set of permissions for the user
     */
    @GetMapping("/{userId}/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or authentication.principal.id == #userId")
    public ResponseEntity<Set<String>> getUserPermissions(@PathVariable Long userId) {
        Set<Permission> permissions = userTypeService.getUserPermissions(userId);
        Set<String> permissionNames = permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(permissionNames);
    }
    
    /**
     * Get roles for a user.
     * 
     * @param userId User ID
     * @return Set of roles for the user
     */
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or authentication.principal.id == #userId")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Long userId) {
        Set<Role> roles = userTypeService.getUserRoles(userId);
        Set<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(roleNames);
    }
    
    /**
     * Check if a user is a super admin.
     * 
     * @param userId User ID
     * @return true if the user is a super admin, false otherwise
     */
    @GetMapping("/{userId}/is-super-admin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Boolean> isSuperAdmin(@PathVariable Long userId) {
        boolean isSuperAdmin = userTypeService.isSuperAdmin(userId);
        return ResponseEntity.ok(isSuperAdmin);
    }
    
    /**
     * Check if a user is an admin.
     * 
     * @param userId User ID
     * @return true if the user is an admin, false otherwise
     */
    @GetMapping("/{userId}/is-admin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Boolean> isAdmin(@PathVariable Long userId) {
        boolean isAdmin = userTypeService.isAdmin(userId);
        return ResponseEntity.ok(isAdmin);
    }
    
    /**
     * Check if a user is a demo user.
     * 
     * @param userId User ID
     * @return true if the user is a demo user, false otherwise
     */
    @GetMapping("/{userId}/is-demo-user")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Boolean> isDemoUser(@PathVariable Long userId) {
        boolean isDemoUser = userTypeService.isDemoUser(userId);
        return ResponseEntity.ok(isDemoUser);
    }
    
    /**
     * Map a list of users to DTOs.
     * 
     * @param users List of users
     * @return List of user DTOs
     */
    private List<PHUserDTO> mapUsersToDTOs(List<User> users) {
        return users.stream()
                .map(UserConverter::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Map a user to a DTO.
     * 
     * @param user User
     * @return User DTO
     */
    private PHUserDTO mapUserToDTO(User user) {
        return UserConverter.toDTO(user);
    }
}