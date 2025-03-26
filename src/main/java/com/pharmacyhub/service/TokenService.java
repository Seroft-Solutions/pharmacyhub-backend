package com.pharmacyhub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing tokens for email verification, password reset, etc.
 */
@Service
public class TokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    
    // Token expiration time in minutes
    @Value("${pharmacyhub.security.token.expiration:60}")
    private int tokenExpirationMinutes;
    
    // Map to store tokens with their expiration time and purpose
    // Using ConcurrentHashMap for thread safety
    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    
    /**
     * Generate a new token for a user
     *
     * @param userId User ID
     * @param purpose Token purpose (e.g., "verify-email", "reset-password")
     * @return Generated token
     */
    public String generateToken(Long userId, String purpose) {
        // Generate a secure token without special characters that might cause URL issues
        String token = generateSecureUrlSafeToken();
        
        // Store token with expiration time
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);
        tokenStore.put(token, new TokenInfo(userId, purpose, expirationTime));
        
        logger.info("Generated {} token for user ID: {}, expires at: {}", purpose, userId, expirationTime);
        
        return token;
    }
    
    /**
     * Generate a secure URL-safe token
     * 
     * @return A URL-safe token string
     */
    private String generateSecureUrlSafeToken() {
        // Generate a UUID-based token (more reliable for URLs)
        return UUID.randomUUID().toString();
    }
    
    /**
     * Validate a token
     *
     * @param token Token to validate
     * @param purpose Expected purpose
     * @return User ID if valid, null otherwise
     */
    public Long validateToken(String token, String purpose) {
        // Add debugging
        logger.debug("Validating token: {}, purpose: {}", token, purpose);
        
        // Check if token exists in store
        TokenInfo tokenInfo = tokenStore.get(token);
        
        if (tokenInfo == null) {
            logger.warn("Token not found in store: {}", token);
            return null;
        }
        
        // Check purpose
        if (!purpose.equals(tokenInfo.purpose)) {
            logger.warn("Token purpose mismatch. Expected: {}, Found: {}", purpose, tokenInfo.purpose);
            return null;
        }
        
        // Check expiration
        if (LocalDateTime.now().isAfter(tokenInfo.expirationTime)) {
            logger.warn("Token expired at: {}", tokenInfo.expirationTime);
            tokenStore.remove(token); // Clean up expired token
            return null;
        }
        
        logger.info("Token validated successfully for user ID: {}, purpose: {}", tokenInfo.userId, purpose);
        return tokenInfo.userId;
    }
    
    /**
     * Invalidate a token (after use)
     *
     * @param token Token to invalidate
     */
    public void invalidateToken(String token) {
        tokenStore.remove(token);
        logger.debug("Token invalidated: {}", token);
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
            logger.debug("Updated user ID for token: {} to: {}", token, userId);
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
        int removedCount = 0;
        
        // Using iterator for safe concurrent removal
        for (Map.Entry<String, TokenInfo> entry : tokenStore.entrySet()) {
            if (now.isAfter(entry.getValue().expirationTime)) {
                tokenStore.remove(entry.getKey());
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            logger.info("Cleaned up {} expired tokens", removedCount);
        }
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
