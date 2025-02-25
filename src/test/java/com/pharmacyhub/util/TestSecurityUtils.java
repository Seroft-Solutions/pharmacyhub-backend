package com.pharmacyhub.util;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.domain.Permission;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods for setting up security context in tests
 */
public class TestSecurityUtils {

    /**
     * Set up security context with test user having the specified role
     */
    public static void setupTestSecurityContext(RoleEnum roleEnum) {
        if (roleEnum == null) {
            roleEnum = RoleEnum.USER;
        }
        
        User testUser = TestDataBuilder.createUser("test@pharmacyhub.pk", "password", mapRoleToUserType(roleEnum));
        
        // Use TestDatabaseSetup.getOrCreateRole here if available, otherwise fallback to TestDataBuilder
        Role userRole = TestDataBuilder.createRole(roleEnum, getDefaultPrecedence(roleEnum));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleEnum.toString()));
        
        // Add special management permissions for ADMIN role
        if (roleEnum == RoleEnum.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE"));
            authorities.add(new SimpleGrantedAuthority("ROLE_MANAGE"));
            authorities.add(new SimpleGrantedAuthority("GROUP_MANAGE"));
            authorities.add(new SimpleGrantedAuthority("ROLE_ASSIGN"));
            authorities.add(new SimpleGrantedAuthority("GROUP_ASSIGN"));
            authorities.add(new SimpleGrantedAuthority("USER_READ"));
            authorities.add(new SimpleGrantedAuthority("GROUP_READ"));
        }
        
        // Get authorities from permissions
        if (userRole.getPermissions() != null) {
            authorities.addAll(userRole.getPermissions().stream()
                .filter(permission -> permission != null && permission.getName() != null)
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList()));
        }
            
        Authentication auth = new UsernamePasswordAuthenticationToken(
            testUser, 
            "password",
            authorities
        );
        
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    /**
     * Set up security context with the provided user
     */
    public static void setSecurityContext(User user) {
        if (user == null) {
            clearSecurityContext();
            return;
        }
        
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Add role-based authorities
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            for (Role role : user.getRoles()) {
                if (role != null) {
                    if (role.getName() != null && !role.getName().isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                    } else if (role.getRoleEnum() != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleEnum().toString()));
                    }
                    
                    // Add special management permissions for ADMIN role
                    if (role.getRoleEnum() == RoleEnum.ADMIN) {
                        authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("ROLE_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("GROUP_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("ROLE_ASSIGN"));
                        authorities.add(new SimpleGrantedAuthority("GROUP_ASSIGN"));
                        authorities.add(new SimpleGrantedAuthority("USER_READ"));
                        authorities.add(new SimpleGrantedAuthority("GROUP_READ"));
                    }
                    
                    // Add authorities from permissions
                    if (role.getPermissions() != null) {
                        authorities.addAll(role.getPermissions().stream()
                            .filter(permission -> permission != null && permission.getName() != null)
                            .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                            .collect(Collectors.toList()));
                    }
                }
            }
        }
        
        // Ensure we have at least one role based on user type
        if (authorities.isEmpty() && user.getUserType() != null) {
            RoleEnum defaultRole = mapUserTypeToRole(user.getUserType());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + defaultRole.toString()));
        }
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user, 
            "password",
            authorities
        );
        
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    /**
     * Clear security context (call after tests)
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
    
    /**
     * Map role enum to user type
     */
    private static UserType mapRoleToUserType(RoleEnum role) {
        if (role == null) {
            return UserType.PHARMACIST;
        }
        
        switch (role) {
            case ADMIN:
                return UserType.ADMIN;
            case SUPER_ADMIN:
                return UserType.SUPER_ADMIN;
            case PHARMACIST:
                return UserType.PHARMACIST;
            case PHARMACY_MANAGER:
                return UserType.PHARMACY_MANAGER;
            case PROPRIETOR:
                return UserType.PROPRIETOR;
            case SALESMAN:
                return UserType.SALESMAN;
            default:
                return UserType.PHARMACIST;
        }
    }
    
    /**
     * Map user type to role enum
     */
    private static RoleEnum mapUserTypeToRole(UserType userType) {
        if (userType == null) {
            return RoleEnum.USER;
        }
        
        switch (userType) {
            case ADMIN:
                return RoleEnum.ADMIN;
            case SUPER_ADMIN:
                return RoleEnum.SUPER_ADMIN;
            case PHARMACIST:
                return RoleEnum.PHARMACIST;
            case PHARMACY_MANAGER:
                return RoleEnum.PHARMACY_MANAGER;
            case PROPRIETOR:
                return RoleEnum.PROPRIETOR;
            case SALESMAN:
                return RoleEnum.SALESMAN;
            default:
                return RoleEnum.USER;
        }
    }
    
    /**
     * Get default precedence for role
     */
    private static int getDefaultPrecedence(RoleEnum role) {
        if (role == null) {
            return 100; // Default to lowest precedence
        }
        
        switch (role) {
            case SUPER_ADMIN:
                return 10;
            case ADMIN:
                return 20;
            case PROPRIETOR:
                return 40;
            case PHARMACY_MANAGER:
                return 60;
            case PHARMACIST:
                return 80;
            case SALESMAN:
                return 90;
            case USER:
                return 100;
            default:
                return 100;
        }
    }
    
    /**
     * Security context factory for @WithMockUserPrincipal annotation
     */
    public static class WithMockUserSecurityContextFactory implements WithSecurityContextFactory<WithMockUserPrincipal> {
        
        @Override
        public SecurityContext createSecurityContext(WithMockUserPrincipal annotation) {
            User testUser = User.builder()
                .id(annotation.id())
                .emailAddress(annotation.email())
                .firstName(annotation.firstName())
                .lastName(annotation.lastName())
                .userType(UserType.PHARMACIST)
                .active(true)
                .verified(true)
                .registered(true)
                .openToConnect(true)
                .accountNonLocked(true)
                .roles(new HashSet<>())
                .groups(new HashSet<>())
                .permissionOverrides(new HashSet<>())
                .build();
                
            // Add default role
            Role role = TestDataBuilder.createRole(RoleEnum.PHARMACIST, getDefaultPrecedence(RoleEnum.PHARMACIST));
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            testUser.setRoles(roles);
            
            // Create authorities
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_PHARMACIST"));
            
            // Add authorities from permissions
            if (role.getPermissions() != null) {
                authorities.addAll(role.getPermissions().stream()
                    .filter(permission -> permission != null && permission.getName() != null)
                    .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                    .collect(Collectors.toList()));
            }
                
            // Create authentication
            Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser,
                "password",
                authorities
            );
            
            // Create security context
            SecurityContext context = new SecurityContextImpl();
            context.setAuthentication(auth);
            return context;
        }
    }
}