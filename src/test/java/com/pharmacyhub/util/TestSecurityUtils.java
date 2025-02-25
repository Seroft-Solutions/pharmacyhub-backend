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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    public static void setupTestSecurityContext(RoleEnum role) {
        User testUser = TestDataBuilder.createUser("test@pharmacyhub.pk", "password", mapRoleToUserType(role));
        
        Role userRole = TestDataBuilder.createRole(role, 1);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString()));
        
        // Add special management permissions for ADMIN role
        if (role == RoleEnum.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE"));
            authorities.add(new SimpleGrantedAuthority("ROLE_MANAGE"));
            authorities.add(new SimpleGrantedAuthority("GROUP_MANAGE"));
            authorities.add(new SimpleGrantedAuthority("ROLE_ASSIGN"));
            authorities.add(new SimpleGrantedAuthority("GROUP_ASSIGN"));
            authorities.add(new SimpleGrantedAuthority("USER_READ"));
            authorities.add(new SimpleGrantedAuthority("GROUP_READ"));
        }
        
        // Get authorities from permissions
        authorities.addAll(userRole.getPermissions().stream()
            .map(permission -> new SimpleGrantedAuthority(permission.getName()))
            .collect(Collectors.toList()));
            
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
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Add role-based authorities
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            for (Role role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                
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
                authorities.addAll(role.getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                    .collect(Collectors.toList()));
            }
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
                .roles(new HashSet<>())
                .build();
                
            // Add default role
            Role role = TestDataBuilder.createRole(RoleEnum.PHARMACIST, 1);
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            testUser.setRoles(roles);
            
            // Create authorities
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_PHARMACIST"));
            
            // Add authorities from permissions
            authorities.addAll(role.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList()));
                
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