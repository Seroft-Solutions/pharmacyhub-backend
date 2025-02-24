package com.pharmacyhub.security;

import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtHelperTest extends BaseIntegrationTest {

    @Autowired
    private JwtHelper jwtHelper;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = TestDataBuilder.createUser("test@pharmacyhub.pk", "password", UserType.PHARMACIST);
        
        // Add role to user
        Role role = TestDataBuilder.createRole(RoleEnum.USER, 5);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        testUser.setRoles(roles);
    }

    @Test
    void testGenerateToken() {
        // Generate token
        String token = jwtHelper.generateToken(testUser);
        
        // Verify token is not null or empty
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetUsernameFromToken() {
        // Generate token
        String token = jwtHelper.generateToken(testUser);
        
        // Get username from token
        String username = jwtHelper.getUsernameFromToken(token);
        
        // Verify username
        assertEquals(testUser.getEmailAddress(), username);
    }

    @Test
    void testGetExpirationDateFromToken() {
        // Generate token
        String token = jwtHelper.generateToken(testUser);
        
        // Get expiration date from token
        Date expirationDate = jwtHelper.getExpirationDateFromToken(token);
        
        // Verify expiration date is in the future
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void testValidateToken() {
        // Generate token
        String token = jwtHelper.generateToken(testUser);
        
        // Validate token
        boolean isValid = jwtHelper.validateToken(token, testUser);
        
        // Verify token is valid
        assertTrue(isValid);
    }

    @Test
    void testInvalidTokenWithDifferentUser() {
        // Generate token for test user
        String token = jwtHelper.generateToken(testUser);
        
        // Create different user
        User otherUser = TestDataBuilder.createUser("other@pharmacyhub.pk", "password", UserType.PHARMACIST);
        
        // Validate token with different user
        boolean isValid = jwtHelper.validateToken(token, otherUser);
        
        // Verify token is invalid for different user
        assertFalse(isValid);
    }

    @Test
    void testGetAllClaimsFromToken() {
        // Generate token
        String token = jwtHelper.generateToken(testUser);
        
        // Get claims from token
        String subject = jwtHelper.getClaimFromToken(token, claims -> claims.getSubject());
        
        // Verify subject claim
        assertEquals(testUser.getEmailAddress(), subject);
    }

    @Test
    void testTokenExpiration() {
        // We can't easily test actual expiration in unit test
        // But we can verify the expiration date is set according to the constant
        
        // Generate token
        String token = jwtHelper.generateToken(testUser);
        
        // Get expiration date
        Date expiration = jwtHelper.getExpirationDateFromToken(token);
        
        // Current date
        Date now = new Date();
        
        // Calculate expected expiration time (milliseconds)
        long expectedExpirationTime = now.getTime() + JwtHelper.JWT_TOKEN_VALIDITY * 1000;
        
        // Allow for a few seconds difference due to processing time
        long allowedDifferenceMs = 5000; // 5 seconds
        
        // Verify expiration is within expected range
        assertTrue(Math.abs(expiration.getTime() - expectedExpirationTime) < allowedDifferenceMs);
    }
}
