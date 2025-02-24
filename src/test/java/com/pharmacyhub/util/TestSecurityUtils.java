package com.pharmacyhub.util;

import com.pharmacyhub.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

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
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            
            User user = User.builder()
                    .id(annotation.id())
                    .emailAddress(annotation.email())
                    .firstName(annotation.firstName())
                    .lastName(annotation.lastName())
                    .active(true)
                    .build();
                    
            Authentication auth = createAuthentication(user);
            context.setAuthentication(auth);
            return context;
        }
    }
}
