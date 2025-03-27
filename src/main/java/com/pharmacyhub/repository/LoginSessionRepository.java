package com.pharmacyhub.repository;

import com.pharmacyhub.entity.session.LoginSession;
import com.pharmacyhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing login sessions
 */
@Repository
public interface LoginSessionRepository extends JpaRepository<LoginSession, UUID> {
    
    /**
     * Find all active sessions for a user
     */
    @Query("SELECT ls FROM LoginSession ls WHERE ls.user.id = :userId AND ls.active = true")
    List<LoginSession> findActiveSessionsByUserId(Long userId);
    
    /**
     * Find all sessions for a user
     */
    List<LoginSession> findAllByUserOrderByLoginTimeDesc(User user);
    
    /**
     * Find session by user and device ID
     */
    Optional<LoginSession> findByUserAndDeviceId(User user, String deviceId);
    
    /**
     * Check if a session exists for a user and device ID
     */
    boolean existsByUserAndDeviceId(User user, String deviceId);
    
    /**
     * Count active sessions for a user
     */
    @Query("SELECT COUNT(ls) FROM LoginSession ls WHERE ls.user.id = :userId AND ls.active = true")
    long countActiveSessionsByUserId(Long userId);
    
    /**
     * Count recent device changes for a user within days
     *
     * Using native query with PostgreSQL interval to avoid JPQL/HQL date arithmetic issues
     */
    @Query(value = "SELECT COUNT(DISTINCT device_id) FROM login_sessions WHERE user_id = ?1 AND login_time > (NOW() - (?2 * INTERVAL '1 day'))", nativeQuery = true)
    int countRecentDeviceChanges(Long userId, int days);
    
    /**
     * Count recent IP changes for a user within days
     *
     * Using native query with PostgreSQL interval to avoid JPQL/HQL date arithmetic issues
     */
    @Query(value = "SELECT COUNT(DISTINCT ip_address) FROM login_sessions WHERE user_id = ?1 AND login_time > (NOW() - (?2 * INTERVAL '1 day'))", nativeQuery = true)
    int countRecentIpChanges(Long userId, int days);
    
    /**
     * Find suspicious sessions (multiple logins from different IPs/countries)
     */
    @Query("SELECT ls FROM LoginSession ls WHERE ls.user.id = :userId AND ls.active = true " +
           "AND ls.ipAddress != :currentIpAddress")
    List<LoginSession> findSuspiciousSessions(Long userId, String currentIpAddress);
    
    /**
     * Deactivate a specific session
     */
    @Modifying
    @Transactional
    @Query("UPDATE LoginSession ls SET ls.active = false WHERE ls.id = :sessionId")
    void deactivateSession(UUID sessionId);
    
    /**
     * Deactivate all sessions for a user except the current one
     */
    @Modifying
    @Transactional
    @Query("UPDATE LoginSession ls SET ls.active = false WHERE ls.user.id = :userId AND ls.id != :currentSessionId")
    void deactivateOtherSessions(Long userId, UUID currentSessionId);
    
    /**
     * Find all sessions created before the specified date
     */
    List<LoginSession> findByLoginTimeBefore(ZonedDateTime date);
    
    /**
     * Find all active sessions created before the specified date
     */
    List<LoginSession> findByActiveIsTrueAndLoginTimeBefore(ZonedDateTime date);
    
    /**
     * Find sessions by date range
     */
    List<LoginSession> findByLoginTimeBetween(ZonedDateTime startDate, ZonedDateTime endDate);
    
    /**
     * Find sessions from a specific country
     */
    List<LoginSession> findByCountryAndActiveIsTrue(String country);
    
    /**
     * Find sessions that need OTP verification
     */
    List<LoginSession> findByRequiresOtpIsTrueAndOtpVerifiedIsFalse();
    
    /**
     * Mark all user sessions as requiring OTP verification
     */
    @Modifying
    @Transactional
    @Query("UPDATE LoginSession ls SET ls.requiresOtp = true WHERE ls.user.id = :userId")
    void requireOtpForAllSessions(Long userId);
    
    /**
     * Mark a session as OTP verified
     */
    @Modifying
    @Transactional
    @Query("UPDATE LoginSession ls SET ls.otpVerified = true WHERE ls.id = :sessionId")
    void markSessionOtpVerified(UUID sessionId);
    
    /**
     * Update session last active time
     */
    @Modifying
    @Transactional
    @Query("UPDATE LoginSession ls SET ls.lastActive = :now WHERE ls.id = :sessionId")
    void updateLastActive(UUID sessionId, ZonedDateTime now);
    
    /**
     * Save login session with JSON metadata
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO login_sessions (id, user_id, device_id, ip_address, country, user_agent, login_time, last_active, active, metadata, requires_otp, otp_verified) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, CAST(?10 AS jsonb), ?11, ?12)", nativeQuery = true)
    void saveLoginSessionWithJsonMetadata(UUID id, Long userId, String deviceId, String ipAddress, String country, 
                                     String userAgent, ZonedDateTime loginTime, ZonedDateTime lastActive, 
                                     boolean active, String metadata, boolean requiresOtp, boolean otpVerified);
}
