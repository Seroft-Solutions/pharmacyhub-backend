package com.pharmacyhub.security.service;

import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.JwtHelper;
import com.pharmacyhub.security.infrastructure.exception.UnverifiedAccountException;
import com.pharmacyhub.service.UserService;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.security.service.UserRoleService;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.auth.oauth2.TokenResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.pharmacyhub.dto.UserDTO;

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
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Value("${google.oauth.client-id:1}")
    private String googleClientId;
    
    @Value("${google.oauth.client-secret:1}")
    private String googleClientSecret;
    
    @Value("${google.oauth.redirect-uri:http://localhost:3000/auth/callback}")
    private String googleRedirectUri;
    
    @Value("${google.oauth.default-user-role:ROLE_USER}")
    private String defaultUserRole;
    
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
        
        // Check if user is verified
        if (!authenticatedUser.isVerified()) {
            logger.warn("Attempted login to unverified account: {}", username);
            throw new UnverifiedAccountException("Your account is not verified. Please check your email for verification instructions.");
        }
        
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
    
    /**
     * Extract user ID from a JWT token
     * 
     * @param token The JWT token
     * @return The user ID or null if token is invalid
     */
    public Long getUserIdFromToken(String token) {
        try {
            return jwtHelper.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.error("Error extracting user ID from token", e);
            return null;
        }
    }
    
    /**
     * Process social login request from OAuth providers
     * 
     * @param code The authorization code received from the OAuth provider
     * @param callbackUrl The callback URL used in the OAuth flow
     * @return The authenticated user (existing or newly created)
     * @throws IOException If there is a problem with the HTTP transport
     * @throws GeneralSecurityException If there is a security issue with the token verification
     */
    public User processSocialLogin(String code, String callbackUrl) throws IOException, GeneralSecurityException {
        logger.info("Processing social login with code: [PRESENT] and callback URL: {}", callbackUrl);
        
        // Determine which provider based on callback URL
        if (callbackUrl.toLowerCase().contains("google") || callbackUrl.toLowerCase().contains("auth/callback")) {
            return processGoogleLogin(code, callbackUrl);
        } else if (callbackUrl.toLowerCase().contains("facebook")) {
            // For future implementation
            throw new UnsupportedOperationException("Facebook login not implemented yet");
        } else {
            throw new IllegalArgumentException("Unsupported OAuth provider");
        }
    }
    
    /**
     * Process Google login using the authorization code
     * 
     * @param code The authorization code from Google
     * @param redirectUri The redirect URI used in the Google OAuth flow
     * @return The authenticated user
     * @throws IOException If there is a problem with the HTTP transport
     * @throws GeneralSecurityException If there is a security issue with the token verification
     */
    private User processGoogleLogin(String code, String redirectUri) throws IOException, GeneralSecurityException {
        logger.debug("Processing Google login with code: [PRESENT] and redirect URI: {}", redirectUri);
        
        // First, exchange the authorization code for an ID token
        String idTokenString = exchangeGoogleAuthCode(code, redirectUri);
        if (idTokenString == null) {
            throw new SecurityException("Failed to exchange Google authorization code for tokens");
        }
        
        // Verify the ID token
        GoogleIdToken idToken = verifyGoogleIdToken(idTokenString);
        if (idToken == null) {
            throw new SecurityException("Invalid Google ID token");
        }
        
        // Extract user info from the ID token
        Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");
        String locale = (String) payload.get("locale");
        String familyName = (String) payload.get("family_name");
        String givenName = (String) payload.get("given_name");
        
        // Log user information (excluding sensitive data)
        logger.info("Google user info - Email: {}, Name: {}, Verified: {}", email, name, emailVerified);
        
        // Find existing user or create a new one
        User user = userService.findByEmail(email);
        
        if (user == null) {
            // Create a new user
            logger.info("Creating new user for Google account: {}", email);
            
            user = new User();
            user.setEmailAddress(email);
            user.setFirstName(givenName != null ? givenName : "");
            user.setLastName(familyName != null ? familyName : "");
            user.setUsername(email); // Use email as username
            
            // Generate a secure random password
            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(randomPassword));
            
            // Mark as verified since Google verifies emails
            user.setVerified(emailVerified);
            user.setEnabled(true);
            
            // Add profile picture URL if available
            if (pictureUrl != null && !pictureUrl.isEmpty()) {
                user.setProfilePictureUrl(pictureUrl);
            }
            
            // Save the user
            UserDTO userDTO = convertToUserDTO(user);
            userService.saveUser(userDTO);
            
            // Refresh the user from the database to ensure we have all properties
            user = userService.findByEmail(email);
            
            // Assign the default role
            userRoleService.assignRoleToUser(user.getId(), defaultUserRole);
        } else {
            logger.info("Found existing user for Google account: {}", email);
            
            // Update user information if needed
            boolean updated = false;
            
            // Update verification status if not already verified
            if (!user.isVerified() && emailVerified) {
                user.setVerified(true);
                updated = true;
            }
            
            // Update profile picture if new one available
            if (pictureUrl != null && !pictureUrl.isEmpty() && 
                (user.getProfilePictureUrl() == null || !user.getProfilePictureUrl().equals(pictureUrl))) {
                user.setProfilePictureUrl(pictureUrl);
                updated = true;
            }
            
            // Save if updated
            if (updated) {
                UserDTO userDTO = convertToUserDTO(user);
                userService.saveUser(userDTO);
                
                // Refresh the user from the database
                user = userService.findByEmail(email);
            }
        }
        
        return user;
    }
    
    /**
     * Google OAuth 2.0 Flow:
     * 1. User clicks "Login with Google" on frontend
     * 2. Frontend redirects to Google OAuth consent screen
     * 3. User grants permissions and Google redirects back with an authorization code
     * 4. Frontend sends this authorization code to our backend
     * 5. Backend exchanges the code for tokens (ID token, access token, refresh token)
     * 6. Backend verifies the ID token and extracts user information
     * 7. Backend creates/updates the user account and generates a JWT for authentication
     */

    /**
     * Exchange Google authorization code for tokens
     *
     * @param code The authorization code received from Google
     * @param redirectUri The redirect URI used in the OAuth flow
     * @return The ID token string or null if exchange fails
     */
    private String exchangeGoogleAuthCode(String code, String redirectUri) {
        try {
            logger.debug("Exchanging Google auth code for tokens with redirect URI: {}", redirectUri);
            
            // Create HTTP transport and JSON factory
            NetHttpTransport httpTransport = new NetHttpTransport();
            GsonFactory jsonFactory = new GsonFactory();
            
            // Set up the Google OAuth 2.0 endpoint
            logger.info("Google OAuth token exchange - Client ID: {}, Redirect URI: {}", 
                googleClientId.substring(0, 8) + "...", redirectUri);
                
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                googleClientId,
                googleClientSecret,
                code,
                redirectUri)  // Use the actual redirect URI from the request
                .execute();
                
            logger.debug("Successfully exchanged authorization code for tokens");
            
            // Return the ID token
            return tokenResponse.getIdToken();
        } catch (Exception e) {
            logger.error("Error exchanging Google auth code for tokens: {}", e.getMessage(), e);
            // Log more details if available
            if (e instanceof TokenResponseException) {
                TokenResponseException tre = (TokenResponseException) e;
                logger.error("Token response error - Status code: {}, Details: {}", 
                    tre.getStatusCode(), tre.getDetails());
            }
            return null;
        }
    }

    /**
     * Verify Google ID token
     *
     * @param idTokenString The ID token string from Google
     * @return The verified GoogleIdToken, or null if invalid
     */
    private GoogleIdToken verifyGoogleIdToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            
            return verifier.verify(idTokenString);
        } catch (Exception e) {
            logger.error("Error verifying Google ID token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract user ID from JWT token (extended version with enhanced error logging)
     * 
     * @param token JWT token
     * @return User ID extracted from token, or null if invalid
     */
    /**
     * Convert User entity to UserDTO
     *
     * @param user The User entity to convert
     * @return The converted UserDTO
     */
    private UserDTO convertToUserDTO(User user) {
        if (user == null) return null;
        
        return UserDTO.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .emailAddress(user.getEmailAddress())
            .contactNumber(user.getContactNumber())
            .userType(user.getUserType())
            .registered(user.isRegistered())
            .openToConnect(user.isOpenToConnect())
            // Add any other fields that need to be mapped
            .build();
    }
    
    public Long extractUserIdFromToken(String token) {
        try {
            return jwtHelper.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from token", e);
            return null;
        }
    }
    
    /**
     * Encode a password using the system's password encoder
     * 
     * @param rawPassword The raw password to encode
     * @return The encoded password
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * Reload user with updated roles and permissions
     * 
     * @param userId The user ID
     * @return The user with updated roles and permissions
     */
    public User reloadUserWithRoles(Long userId) {
        logger.debug("Reloading user {} with updated roles", userId);
        return userService.getUserById(userId);
    }
}
