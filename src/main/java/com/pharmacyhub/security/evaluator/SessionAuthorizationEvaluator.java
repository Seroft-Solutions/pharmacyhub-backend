package com.pharmacyhub.security.evaluator;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.session.LoginSession;
import com.pharmacyhub.repository.LoginSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Authorization evaluator for session operations
 */
@Component("sessionAuthorizationEvaluator")
@RequiredArgsConstructor
public class SessionAuthorizationEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionAuthorizationEvaluator.class);
    
    private final LoginSessionRepository loginSessionRepository;
    
    /**
     * Check if a session belongs to the current user
     * 
     * @param sessionId Session ID
     * @param userDetails Current user details
     * @return True if the session belongs to the current user
     */
    public boolean isOwnSession(UUID sessionId, UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        
        Optional<LoginSession> session = loginSessionRepository.findById(sessionId);
        
        if (session.isEmpty()) {
            return false;
        }
        
        Long userId = getUserId(userDetails);
        
        if (userId == null) {
            return false;
        }
        
        return userId.equals(session.get().getUser().getId());
    }
    
    /**
     * Get user ID from UserDetails
     * 
     * @param userDetails User details
     * @return User ID or null if not available
     */
    public Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof User) {
            return ((User) userDetails).getId();
        }
        
        return null;
    }
}
