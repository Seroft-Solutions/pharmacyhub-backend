package com.pharmacyhub.security.filter;

import com.pharmacyhub.repository.LoginSessionRepository;
import com.pharmacyhub.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to enforce single session policy by checking if the current session is still valid
 */
@Component
@RequiredArgsConstructor
public class SessionInvalidationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionInvalidationFilter.class);
    
    private final JwtTokenProvider tokenProvider;
    private final LoginSessionRepository loginSessionRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = tokenProvider.resolveToken(request);
        String sessionId = request.getHeader("X-Session-ID");
        
        if (token != null && sessionId != null) {
            try {
                UUID uuid = UUID.fromString(sessionId);
                
                // Check if this session is still active in the database
                boolean isSessionActive = loginSessionRepository.findById(uuid)
                    .map(session -> session.isActive() && session.isOtpVerified())
                    .orElse(false);
                
                if (!isSessionActive) {
                    logger.warn("Rejecting request with invalid session ID: {}", sessionId);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Session has been invalidated\",\"code\":\"SESSION_INVALIDATED\"}");
                    return;
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid session ID format: {}", sessionId);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
