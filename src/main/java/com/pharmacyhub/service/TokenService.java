package com.pharmacyhub.service;

import com.pharmacyhub.entity.Token;
import com.pharmacyhub.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing tokens for email verification, password reset, etc.
 */
@Service
public class TokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    
    @Autowired
    private TokenRepository tokenRepository;
    
    // Token expiration time in minutes
    @Value("${pharmacyhub.security.token.expiration:60}")
    private int tokenExpirationMinutes;
    
    /**
     * Generate a new token for a user
     *
     * @param userId User ID
     * @param purpose Token purpose (e.g., "verify-email", "reset-password")
     * @return Generated token
     */
    @Transactional
    public String generateToken(Long userId, String purpose) {
        // Generate a secure token without special characters that might cause URL issues
        String token = generateSecureUrlSafeToken();
        
        // Calculate expiration time
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);
        
        // Create and save token entity
        Token tokenEntity = Token.builder()
                .token(token)
                .userId(userId)
                .purpose(purpose)
                .expirationTime(expirationTime)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Save the token to the database
        tokenRepository.save(tokenEntity);
        
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
    @Transactional(readOnly = true)
    public Long validateToken(String token, String purpose) {
        // Add debugging
        logger.debug("Validating token: {}, purpose: {}", token, purpose);
        
        // Find token in database
        Optional<Token> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            logger.warn("Token not found in store: {}", token);
            return null;
        }
        
        Token tokenEntity = tokenOpt.get();
        
        // Check purpose
        if (!purpose.equals(tokenEntity.getPurpose())) {
            logger.warn("Token purpose mismatch. Expected: {}, Found: {}", purpose, tokenEntity.getPurpose());
            return null;
        }
        
        // Check expiration
        if (tokenEntity.isExpired()) {
            logger.warn("Token expired at: {}", tokenEntity.getExpirationTime());
            // Clean up expired token
            tokenRepository.delete(tokenEntity);
            return null;
        }
        
        logger.info("Token validated successfully for user ID: {}, purpose: {}", tokenEntity.getUserId(), purpose);
        return tokenEntity.getUserId();
    }
    
    /**
     * Invalidate a token (after use)
     *
     * @param token Token to invalidate
     */
    @Transactional
    public void invalidateToken(String token) {
        tokenRepository.deleteByToken(token);
        logger.debug("Token invalidated: {}", token);
    }
    
    /**
     * Invalidate all tokens for a user
     *
     * @param userId User ID
     */
    @Transactional
    public void invalidateUserTokens(Long userId) {
        tokenRepository.deleteByUserId(userId);
        logger.debug("All tokens invalidated for user ID: {}", userId);
    }
    
    /**
     * Invalidate all tokens for a user with a specific purpose
     *
     * @param userId User ID
     * @param purpose Token purpose
     */
    @Transactional
    public void invalidateUserTokensByPurpose(Long userId, String purpose) {
        tokenRepository.deleteByUserIdAndPurpose(userId, purpose);
        logger.debug("All {} tokens invalidated for user ID: {}", purpose, userId);
    }
    
    /**
     * Update the user ID associated with a token (for tokens created before knowing the user ID)
     *
     * @param token Token to update
     * @param userId New user ID
     * @return true if token was updated, false if token not found
     */
    @Transactional
    public boolean updateTokenUserId(String token, Long userId) {
        Optional<Token> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            Token tokenEntity = tokenOpt.get();
            tokenEntity.setUserId(userId);
            tokenRepository.save(tokenEntity);
            logger.debug("Updated user ID for token: {} to: {}", token, userId);
            return true;
        }
        return false;
    }
    
    /**
     * Clean up expired tokens
     * This method is called automatically by a scheduled task
     */
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = tokenRepository.countExpiredTokens(now);
        
        if (expiredCount > 0) {
            int deletedCount = tokenRepository.deleteByExpirationTimeBefore(now);
            logger.info("Cleaned up {} expired tokens", deletedCount);
        }
    }
    
    /**
     * Scheduled task to clean up expired tokens
     * Runs every hour by default
     */
    @Scheduled(fixedRateString = "${pharmacyhub.security.token.cleanup-interval:3600000}")
    public void scheduledCleanupExpiredTokens() {
        try {
            cleanupExpiredTokens();
        } catch (Exception e) {
            logger.error("Error cleaning up expired tokens", e);
        }
    }
    
    /**
     * Check if a token exists
     *
     * @param token Token to check
     * @return true if token exists
     */
    @Transactional(readOnly = true)
    public boolean tokenExists(String token) {
        return tokenRepository.existsByToken(token);
    }
}
