package com.pharmacyhub.repository;

import com.pharmacyhub.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing security tokens
 */
@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    /**
     * Find token by token value
     * 
     * @param token Token value
     * @return Optional containing Token if found
     */
    Optional<Token> findByToken(String token);
    
    /**
     * Find tokens by user ID
     * 
     * @param userId User ID
     * @return List of tokens for the user
     */
    List<Token> findByUserId(Long userId);
    
    /**
     * Find tokens by user ID and purpose
     * 
     * @param userId User ID
     * @param purpose Token purpose
     * @return List of tokens for the user with specified purpose
     */
    List<Token> findByUserIdAndPurpose(Long userId, String purpose);
    
    /**
     * Find tokens by purpose
     * 
     * @param purpose Token purpose
     * @return List of tokens with specified purpose
     */
    List<Token> findByPurpose(String purpose);
    
    /**
     * Delete token by token value
     * 
     * @param token Token value
     */
    void deleteByToken(String token);
    
    /**
     * Delete expired tokens
     * 
     * @param time Current time to compare against expiration
     * @return Number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM Token t WHERE t.expirationTime < :time")
    int deleteByExpirationTimeBefore(@Param("time") LocalDateTime time);
    
    /**
     * Delete all tokens for a user
     * 
     * @param userId User ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * Delete all tokens for a user with specified purpose
     * 
     * @param userId User ID
     * @param purpose Token purpose
     */
    void deleteByUserIdAndPurpose(Long userId, String purpose);
    
    /**
     * Check if token exists
     * 
     * @param token Token value
     * @return true if token exists
     */
    boolean existsByToken(String token);
    
    /**
     * Count expired tokens
     * 
     * @param time Current time to compare against expiration
     * @return Number of expired tokens
     */
    @Query("SELECT COUNT(t) FROM Token t WHERE t.expirationTime < :time")
    int countExpiredTokens(@Param("time") LocalDateTime time);
}
