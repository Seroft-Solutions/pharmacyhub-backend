package com.pharmacyhub.security.jwt;

import com.pharmacyhub.security.JwtHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Provider for JWT token resolution and validation
 * Acts as an adapter between JwtHelper and filters that need token functionality
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    private final JwtHelper jwtHelper;
    
    /**
     * Resolve token from HTTP request
     * @param request HTTP request
     * @return Resolved token or null
     */
    public String resolveToken(HttpServletRequest request) {
        String requestHeader = request.getHeader("Authorization");
        
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            return requestHeader.substring(7);
        }
        
        return null;
    }
    
    /**
     * Get user ID from token
     * @param token JWT token
     * @return User ID or null if invalid
     */
    public Long getUserIdFromToken(String token) {
        try {
            return jwtHelper.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Validate if token is valid
     * @param token JWT token
     * @return True if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Basic validation - check if we can parse the token
            return jwtHelper.getClaimFromToken(token, claims -> true);
        } catch (Exception e) {
            return false;
        }
    }
}
