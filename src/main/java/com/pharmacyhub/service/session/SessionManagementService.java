package com.pharmacyhub.service.session;

import com.pharmacyhub.dto.session.LoginSessionDTO;
import com.pharmacyhub.dto.session.SessionFilterCriteriaDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.session.LoginSession;
import com.pharmacyhub.repository.LoginSessionRepository;
import com.pharmacyhub.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing login sessions
 */
@Service
@RequiredArgsConstructor
public class SessionManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManagementService.class);
    
    private final LoginSessionRepository loginSessionRepository;
    private final UserRepository userRepository;
    
    @Value("${pharmacyhub.security.session.session-timeout-days:30}")
    private int sessionTimeoutDays;
    
    /**
     * Get sessions for a user with optional filtering
     * 
     * @param userId User ID
     * @param criteria Filter criteria
     * @return List of session DTOs
     */
    @Transactional(readOnly = true)
    public List<LoginSessionDTO> getUserSessions(Long userId, SessionFilterCriteriaDTO criteria) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        List<LoginSession> sessions = loginSessionRepository.findAllByUserOrderByLoginTimeDesc(user);
        
        // Apply filters if specified
        if (criteria != null) {
            sessions = filterSessions(sessions, criteria);
        }
        
        return mapToDto(sessions);
    }
    
    /**
     * Get all sessions with optional filtering (admin only)
     * 
     * @param criteria Filter criteria
     * @return List of session DTOs
     */
    @Transactional(readOnly = true)
    public List<LoginSessionDTO> getAllSessions(SessionFilterCriteriaDTO criteria) {
        // Base query - get all sessions
        List<LoginSession> sessions = loginSessionRepository.findAll();
        
        // Apply filters if specified
        if (criteria != null) {
            sessions = filterSessions(sessions, criteria);
        }
        
        return mapToDto(sessions);
    }
    
    /**
     * Filter sessions based on criteria
     * 
     * @param sessions List of sessions
     * @param criteria Filter criteria
     * @return Filtered list of sessions
     */
    private List<LoginSession> filterSessions(List<LoginSession> sessions, SessionFilterCriteriaDTO criteria) {
        List<LoginSession> filteredSessions = new ArrayList<>(sessions);
        
        // Filter by userId if specified
        if (criteria.getUserId() != null) {
            filteredSessions.removeIf(session -> !session.getUser().getId().equals(criteria.getUserId()));
        }
        
        // Filter by active status if specified
        if (criteria.getActive() != null) {
            filteredSessions.removeIf(session -> session.isActive() != criteria.getActive());
        }
        
        // Filter by country if specified
        if (criteria.getCountry() != null && !criteria.getCountry().isBlank()) {
            filteredSessions.removeIf(session -> 
                session.getCountry() == null || !session.getCountry().equals(criteria.getCountry()));
        }
        
        // Filter by date range if specified
        if (criteria.getFromDate() != null && !criteria.getFromDate().isBlank()) {
            ZonedDateTime fromDate = ZonedDateTime.parse(criteria.getFromDate() + "T00:00:00Z", 
                DateTimeFormatter.ISO_DATE_TIME);
            filteredSessions.removeIf(session -> session.getLoginTime().isBefore(fromDate));
        }
        
        if (criteria.getToDate() != null && !criteria.getToDate().isBlank()) {
            ZonedDateTime toDate = ZonedDateTime.parse(criteria.getToDate() + "T23:59:59Z", 
                DateTimeFormatter.ISO_DATE_TIME);
            filteredSessions.removeIf(session -> session.getLoginTime().isAfter(toDate));
        }
        
        // Filter by OTP requirements if specified
        if (criteria.getRequiresOtp() != null) {
            filteredSessions.removeIf(session -> session.isRequiresOtp() != criteria.getRequiresOtp());
        }
        
        return filteredSessions;
    }
    
    /**
     * Get a specific session by ID
     * 
     * @param sessionId Session ID
     * @return Session DTO
     */
    @Transactional(readOnly = true)
    public LoginSessionDTO getSession(UUID sessionId) {
        LoginSession session = loginSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
        
        return mapToDto(session);
    }
    
    /**
     * Terminate a specific session
     * 
     * @param sessionId Session ID
     */
    @Transactional
    public void terminateSession(UUID sessionId) {
        loginSessionRepository.deactivateSession(sessionId);
        logger.debug("Session terminated: {}", sessionId);
    }
    
    /**
     * Terminate all sessions for a user except the current one
     * 
     * @param userId User ID
     * @param currentSessionId Current session ID to keep active
     */
    @Transactional
    public void terminateOtherSessions(Long userId, UUID currentSessionId) {
        loginSessionRepository.deactivateOtherSessions(userId, currentSessionId);
        logger.debug("Terminated other sessions for user: {}, keeping: {}", userId, currentSessionId);
    }
    
    /**
     * Mark a user as requiring OTP verification for all sessions
     * 
     * @param userId User ID
     */
    @Transactional
    public void requireOtpVerification(Long userId) {
        loginSessionRepository.requireOtpForAllSessions(userId);
        logger.debug("OTP verification required for all sessions of user: {}", userId);
    }
    
    /**
     * Mark a session as OTP verified
     * 
     * @param sessionId Session ID
     */
    @Transactional
    public void markSessionOtpVerified(UUID sessionId) {
        loginSessionRepository.markSessionOtpVerified(sessionId);
        logger.debug("Session marked as OTP verified: {}", sessionId);
    }
    
    /**
     * Update session last active time
     * 
     * @param sessionId Session ID
     */
    @Transactional
    public void updateLastActive(UUID sessionId) {
        loginSessionRepository.updateLastActive(sessionId);
    }
    
    /**
     * Get suspicious sessions (different IPs/locations)
     * 
     * @return List of suspicious session DTOs
     */
    @Transactional(readOnly = true)
    public List<LoginSessionDTO> getSuspiciousSessions() {
        List<LoginSession> allSessions = loginSessionRepository.findAll();
        
        // Group sessions by user
        Map<Long, List<LoginSession>> sessionsByUser = allSessions.stream()
            .filter(LoginSession::isActive)
            .collect(Collectors.groupingBy(s -> s.getUser().getId()));
        
        // Find users with multiple locations/IPs
        List<LoginSession> suspiciousSessions = new ArrayList<>();
        
        for (List<LoginSession> userSessions : sessionsByUser.values()) {
            if (userSessions.size() <= 1) {
                continue;
            }
            
            // Check for different countries or IPs
            Set<String> countries = userSessions.stream()
                .map(LoginSession::getCountry)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            Set<String> ips = userSessions.stream()
                .map(LoginSession::getIpAddress)
                .collect(Collectors.toSet());
            
            if (countries.size() > 1 || ips.size() > 1) {
                suspiciousSessions.addAll(userSessions);
            }
        }
        
        return mapToDto(suspiciousSessions);
    }
    
    /**
     * Scheduled task to clean up old sessions
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public void cleanupOldSessions() {
        ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(sessionTimeoutDays);
        List<LoginSession> oldSessions = loginSessionRepository.findByLoginTimeBefore(cutoffDate);
        
        if (!oldSessions.isEmpty()) {
            oldSessions.forEach(session -> session.setActive(false));
            loginSessionRepository.saveAll(oldSessions);
            logger.info("Cleaned up {} old sessions older than {} days", oldSessions.size(), sessionTimeoutDays);
        }
    }
    
    /**
     * Map entity to DTO
     */
    private LoginSessionDTO mapToDto(LoginSession session) {
        return LoginSessionDTO.builder()
            .id(session.getId())
            .userId(session.getUser().getId())
            .deviceId(session.getDeviceId())
            .ipAddress(session.getIpAddress())
            .country(session.getCountry())
            .userAgent(session.getUserAgent())
            .loginTime(session.getLoginTime())
            .lastActive(session.getLastActive())
            .active(session.isActive())
            .requiresOtp(session.isRequiresOtp())
            .otpVerified(session.isOtpVerified())
            .metadata(session.getMetadata())
            .build();
    }
    
    /**
     * Map list of entities to list of DTOs
     */
    private List<LoginSessionDTO> mapToDto(List<LoginSession> sessions) {
        return sessions.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
}
