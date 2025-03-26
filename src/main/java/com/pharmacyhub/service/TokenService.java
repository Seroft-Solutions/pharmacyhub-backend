package com.pharmacyhub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing tokens for email verification, password reset, etc.
 */
@Service
public class TokenService {
    
    // Token expiration time in minutes
    @Value("${pharmacyhub.security.token.expiration:30}")
    private int tokenExpirationMinutes;
    
    // Map to store tokens with their expiration time and purpose
    private final Map<String, TokenInfo> tokenStore = new HashMap<>();
    
    /**
     * Generate a new token for a user
     *
     * @param userId User ID
     * @param purpose Token purpose (e.g., "verify-email", "reset-password")
     * @return Generated token
     */
    public String generateToken(Long userId, String purpose) {
        // Generate a secure random token
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        
        // Encode it as Base64 string for URL-friendliness
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        
        // Create unique token with UUID prefix to avoid collisions
        String token = UUID.randomUUID().toString().substring(0, 8) + "-" + tokenValue;
        
        // Store token with expiration time
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);
        tokenStore.put(token, new TokenInfo(userId, purpose, expirationTime));
        
        return token;
    }
    
    /**
     * Validate a token
     *
     * @param token Token to validate
     * @param purpose Expected purpose
     * @return User ID if valid, null otherwise
     */
    public Long validateToken(String token, String purpose) {
        TokenInfo tokenInfo = tokenStore.get(token);
        
        // Check if token exists and is not expired
        if (tokenInfo != null 
                && purpose.equals(tokenInfo.purpose)
                && LocalDateTime.now().isBefore(tokenInfo.expirationTime)) {
            return tokenInfo.userId;
        }
        
        return null;
    }
    
    /**
     * Invalidate a token (after use)
     *
     * @param token Token to invalidate
     */
    public void invalidateToken(String token) {
        tokenStore.remove(token);
    }
    
    /**
     * Update the user ID associated with a token (for tokens created before knowing the user ID)
     *
     * @param token Token to update
     * @param userId New user ID
     * @return true if token was updated, false if token not found
     */
    public boolean updateTokenUserId(String token, Long userId) {
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo != null) {
            TokenInfo updatedInfo = new TokenInfo(userId, tokenInfo.purpose, tokenInfo.expirationTime);
            tokenStore.put(token, updatedInfo);
            return true;
        }
        return false;
    }
    
    /**
     * Clean up expired tokens
     * This method should be called periodically to avoid memory leaks
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenStore.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expirationTime));
    }
    
    /**
     * Private class to store token information
     */
    private static class TokenInfo {
        final Long userId;
        final String purpose;
        final LocalDateTime expirationTime;
        
        TokenInfo(Long userId, String purpose, LocalDateTime expirationTime) {
            this.userId = userId;
            this.purpose = purpose;
            this.expirationTime = expirationTime;
        }
    }
}
