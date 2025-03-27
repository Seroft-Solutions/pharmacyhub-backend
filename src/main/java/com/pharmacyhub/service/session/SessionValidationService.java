package com.pharmacyhub.service.session;

import com.pharmacyhub.dto.session.LoginValidationRequestDTO;
import com.pharmacyhub.dto.session.LoginValidationResultDTO;
import com.pharmacyhub.dto.session.LoginValidationResultDTO.LoginStatus;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.session.LoginSession;
import com.pharmacyhub.repository.LoginSessionRepository;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.service.geo.GeoIpService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Propagation;

/**
 * Service for validating login sessions and preventing account sharing
 */
@Service
@RequiredArgsConstructor
public class SessionValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionValidationService.class);
    
    private final LoginSessionRepository loginSessionRepository;
    private final UserRepository userRepository;
    private final GeoIpService geoIpService;
    
    @Value("${pharmacyhub.security.session.max-active-sessions:1}")
    private int maxActiveSessions;
    
    @Value("${pharmacyhub.security.session.require-otp-for-new-device:true}")
    private boolean requireOtpForNewDevice;
    
    /**
     * Validate a login attempt
     * 
     * @param request Login validation request with device information
     * @return Login validation result
     */
    @Transactional
    public LoginValidationResultDTO validateLogin(LoginValidationRequestDTO request) {
        logger.debug("Validating login for user ID: {}, device ID: {}", 
            request.getUserId(), request.getDeviceId());
        
        // Get user from repository
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.getUserId()));
        
        // Get country from IP address
        String country = geoIpService.getCountryFromIp(request.getIpAddress());
        
        // Check if this is a new device
        boolean isNewDevice = !loginSessionRepository.existsByUserAndDeviceId(user, request.getDeviceId());
        logger.debug("Is new device: {}", isNewDevice);
        
        // Check for active sessions count
        long activeSessionsCount = loginSessionRepository.countActiveSessionsByUserId(user.getId());
        boolean tooManySessions = activeSessionsCount >= maxActiveSessions;
        logger.debug("Active sessions count: {}, Too many sessions: {}", activeSessionsCount, tooManySessions);
        
        // Check for suspicious location
        boolean isSuspiciousLocation = checkForSuspiciousLocation(user.getId(), country, request.getIpAddress());
        logger.debug("Is suspicious location: {}", isSuspiciousLocation);
        
        // Determine validation result
        LoginValidationResultDTO result;
        
        if (isNewDevice && requireOtpForNewDevice) {
            // Create a new session requiring OTP
            LoginSession session = createOrUpdateSession(user, request, country, true);
            result = LoginValidationResultDTO.builder()
                .status(LoginStatus.NEW_DEVICE)
                .message("Login from a new device detected. Please verify your identity.")
                .requiresOtp(true)
                .sessionId(session.getId())
                .build();
        } else if (isSuspiciousLocation) {
            // Create a session requiring OTP
            LoginSession session = createOrUpdateSession(user, request, country, true);
            result = LoginValidationResultDTO.builder()
                .status(LoginStatus.SUSPICIOUS_LOCATION)
                .message("Login from an unusual location detected. Please verify your identity.")
                .requiresOtp(true)
                .sessionId(session.getId())
                .build();
        } else if (tooManySessions) {
            // Check if there's an existing active session from a different device
            List<LoginSession> activeSessions = loginSessionRepository.findActiveSessionsByUserId(user.getId());
            boolean hasOtherDevice = activeSessions.stream()
                    .anyMatch(s -> !s.getDeviceId().equals(request.getDeviceId()));
            
            if (hasOtherDevice) {
                // Return too many devices error
                result = LoginValidationResultDTO.builder()
                    .status(LoginStatus.TOO_MANY_DEVICES)
                    .message("You are already logged in from another device. Please log out from that device first.")
                    .requiresOtp(false)
                    .build();
            } else {
                // Same device - reactivate session
                LoginSession session = createOrUpdateSession(user, request, country, false);
                result = LoginValidationResultDTO.builder()
                    .status(LoginStatus.OK)
                    .requiresOtp(false)
                    .sessionId(session.getId())
                    .build();
            }
        } else {
            // No active sessions found - create a new one and invalidate others for safety
            LoginSession session = createOrUpdateSession(user, request, country, false);
            
            // If this is a new login on a new device, invalidate any other sessions
            if (maxActiveSessions == 1) {
                // Safety first - invalidate all other sessions
                invalidateOtherSessions(user.getId(), session.getId());
            }
            
            result = LoginValidationResultDTO.builder()
                .status(LoginStatus.OK)
                .requiresOtp(false)
                .sessionId(session.getId())
                .build();
        }
        
        logger.debug("Login validation result: {}", result);
        return result;
    }
    
    /**
     * Check if a location is suspicious for a user
     * 
     * @param userId User ID
     * @param country Country name
     * @param ipAddress IP address
     * @return True if the location is suspicious
     */
    private boolean checkForSuspiciousLocation(Long userId, String country, String ipAddress) {
        // Get the most recent login sessions for this user
        List<LoginSession> recentSessions = loginSessionRepository.findActiveSessionsByUserId(userId);
        
        // If no previous sessions, it's not suspicious
        if (recentSessions.isEmpty()) {
            return false;
        }
        
        // If country is unknown, consider it suspicious
        if (country == null || country.isBlank()) {
            return true;
        }
        
        // Check if this country is different from previous sessions
        for (LoginSession session : recentSessions) {
            // If we have a session from same IP, it's not suspicious
            if (session.getIpAddress().equals(ipAddress)) {
                return false;
            }
            
            // If we have a session from same country, it's less suspicious
            if (session.getCountry() != null && session.getCountry().equals(country)) {
                // But not 100% safe - could implement more sophisticated checks here
                // such as session timing, device patterns, etc.
                return false;
            }
        }
        
        // No matching country or IP found, consider it suspicious
        return true;
    }
    
    /**
     * Invalidate all active sessions for a user
     * 
     * @param userId User ID
     * @return Number of sessions invalidated
     */
    @Transactional
    public int invalidateAllSessions(Long userId) {
        logger.debug("Invalidating all sessions for user ID: {}", userId);
        List<LoginSession> activeSessions = loginSessionRepository.findActiveSessionsByUserId(userId);
        
        if (activeSessions.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (LoginSession session : activeSessions) {
            session.setActive(false);
            loginSessionRepository.save(session);
            count++;
        }
        
        logger.debug("Invalidated {} sessions for user ID: {}", count, userId);
        return count;
    }
    
    /**
     * Invalidate all active sessions for a user except the current one
     * 
     * @param userId User ID
     * @param currentSessionId Current session ID to keep active
     * @return Number of sessions invalidated
     */
    @Transactional
    public int invalidateOtherSessions(Long userId, UUID currentSessionId) {
        logger.debug("Invalidating other sessions for user ID: {} except session ID: {}", userId, currentSessionId);
        List<LoginSession> activeSessions = loginSessionRepository.findActiveSessionsByUserId(userId);
        
        if (activeSessions.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (LoginSession session : activeSessions) {
            if (!session.getId().equals(currentSessionId)) {
                session.setActive(false);
                loginSessionRepository.save(session);
                count++;
            }
        }
        
        logger.debug("Invalidated {} other sessions for user ID: {}", count, userId);
        return count;
    }
    
    /**
     * Invalidate a specific session by ID
     * 
     * @param sessionId Session ID to invalidate
     * @return true if successful, false if session not found or already inactive
     */
    @Transactional
    public boolean invalidateSession(UUID sessionId) {
        logger.debug("Invalidating session: {}", sessionId);
        
        Optional<LoginSession> sessionOpt = loginSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            logger.warn("Session not found: {}", sessionId);
            return false;
        }
        
        LoginSession session = sessionOpt.get();
        if (!session.isActive()) {
            logger.warn("Session already inactive: {}", sessionId);
            return false;
        }
        
        session.setActive(false);
        loginSessionRepository.save(session);
        logger.debug("Session invalidated: {}", sessionId);
        return true;
    }
    
    /**
     * Create a new session or update an existing one
     * 
     * @param user User
     * @param request Login validation request
     * @param country Country name
     * @param requiresOtp Whether OTP verification is required
     * @return Created or updated session
     */
    private LoginSession createOrUpdateSession(User user, LoginValidationRequestDTO request, 
                                             String country, boolean requiresOtp) {
        // Check if a session already exists for this device
        Optional<LoginSession> existingSession = loginSessionRepository.findByUserAndDeviceId(user, request.getDeviceId());
        
        LoginSession session;
        if (existingSession.isPresent()) {
            // Update existing session
            session = existingSession.get();
            session.setIpAddress(request.getIpAddress());
            session.setCountry(country);
            session.setUserAgent(request.getUserAgent());
            session.setLastActive(ZonedDateTime.now());
            session.setActive(true);
            session.setRequiresOtp(requiresOtp);
            session.setOtpVerified(!requiresOtp);
            // Keep existing metadata or update if provided
            if (request.getMetadata() != null) {
                session.setMetadata(ensureValidJson(request.getMetadata()));
            }
            
            // Use standard repository save for updates
            return loginSessionRepository.save(session);
        } else {
            // Create new session using UUID
            UUID sessionId = UUID.randomUUID();
            ZonedDateTime now = ZonedDateTime.now();
            String metadata = ensureValidJson(request.getMetadata());
            
            // Use our custom repository method that handles JSON properly
            loginSessionRepository.saveLoginSessionWithJsonMetadata(
                sessionId,
                user.getId(),
                request.getDeviceId(),
                request.getIpAddress(),
                country,
                request.getUserAgent(),
                now,  // loginTime
                now,  // lastActive
                true, // active
                metadata,
                requiresOtp,
                !requiresOtp  // otpVerified
            );
            
            // Now retrieve the saved session
            return loginSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Failed to save login session"));
        }
    }
    
    /**
     * Ensures that the provided string is valid JSON
     * 
     * @param jsonString String that should be JSON
     * @return Valid JSON string
     */
    private String ensureValidJson(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return "{}";
        }
        
        // Already handled by JsonbConverter, just pass it through
        return jsonString;
    }
}
