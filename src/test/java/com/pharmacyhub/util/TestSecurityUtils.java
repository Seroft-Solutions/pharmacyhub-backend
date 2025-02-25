package com.pharmacyhub.util;

import com.pharmacyhub.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.security.domain.Role;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import java.util.HashSet;
import java.util.Set;

public class TestSecurityUtils {

    /**
     * Sets up the SecurityContext with the provided user
     */
    public static void setSecurityContext(User user) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(user);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Clears the security context
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Creates an authentication object for the given user
     */
    public static Authentication createAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
    }

    /**
     * WithSecurityContext factory implementation for custom annotations
     */
    public static class WithMockUserSecurityContextFactory implements WithSecurityContextFactory<WithMockUserPrincipal> {
        @Override
        public SecurityContext createSecurityContext(WithMockUserPrincipal annotation) {
            // Create a new security context
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            
            // Create user with test role
            Role userRole = TestDataBuilder.createRole(RoleEnum.USER, 1);
            Set<Role> roles = new HashSet<>();
            userRole.setPermissions(new HashSet<>()); // Ensure permissions are initialized
            roles.add(userRole);

            User user = User.builder()
                    .id(annotation.id())
                    .emailAddress(annotation.email())
                    .firstName(annotation.firstName())
                    .lastName(annotation.lastName())
                    .roles(roles)
                    .active(true)
                    .build();

            // Set up authentication with user and authorities
            Authentication auth = createAuthentication(user);
            context.setAuthentication(auth);
            return context;
        }
    }
}
