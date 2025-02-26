package com.pharmacyhub.security.service;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.JwtHelper;
import com.pharmacyhub.security.infrastructure.exception.UnverifiedAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service for handling authentication operations
 */
@Service
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtHelper jwtHelper;
    
    /**
     * Authenticate a user with username and password and verify account status
     * 
     * @param username The username (email)
     * @param password The password
     * @return The authenticated user
     * @throws org.springframework.security.core.AuthenticationException if authentication fails
     * @throws UnverifiedAccountException if the account is not verified
     */
    public User authenticateUser(String username, String password) {
        logger.debug("Attempting to authenticate user: {}", username);
        
        // Create authentication token
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(username, password);
        
        // Authenticate - this will throw an exception if authentication fails
        Authentication authentication = authenticationManager.authenticate(authToken);
        
        // Get the authenticated user
        User authenticatedUser = (User) authentication.getPrincipal();
        

        // TODO:: Check if user is verified
//        if (!authenticatedUser.isVerified()) {
//            throw new UnverifiedAccountException("User account is not verified: " + username);
//        }
        
        logger.debug("User authenticated successfully: {}", username);
        return authenticatedUser;
    }
    
    /**
     * Generate a JWT token for a user
     * 
     * @param user The authenticated user
     * @return The JWT token
     */
    public String generateToken(User user) {
        logger.debug("Generating JWT token for user: {}", user.getUsername());
        return jwtHelper.generateToken(user);
    }
}
