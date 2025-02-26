package com.pharmacyhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.config.TestDatabaseSetup;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.dto.LoggedInUserDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.JwtHelper;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.repository.RoleRepository;
import com.pharmacyhub.security.model.LoginRequest;
import com.pharmacyhub.service.EmailService;
import com.pharmacyhub.util.TestDataBuilder;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtHelper jwtHelper;
    
    @Autowired
    private TestDatabaseSetup testDatabaseSetup;

    @MockBean
    private EmailService emailService;

    private Role userRole;

    @BeforeEach
    void setUp() throws MessagingException {
        // Clear user database and roles
        userRepository.deleteAll();
        testDatabaseSetup.clearAllRoles();
        
        // Create user role using test utility
        userRole = testDatabaseSetup.getOrCreateRole(RoleEnum.USER, 5);
        
        // Mock email service to avoid sending emails during tests
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void testSignup() throws Exception {
        // Create user DTO for signup
        UserDTO userDTO = new UserDTO();
        userDTO.setEmailAddress("test@pharmacyhub.pk");
        userDTO.setPassword("password123");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        
        // Perform signup request
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully. Please check your email for verification."));
        
        // Verify user was created in the database
        assertTrue(userRepository.findByEmailAddress("test@pharmacyhub.pk").isPresent());
    }

    @Test
    void testLogin() throws Exception {
        // Create test user
        User user = TestDataBuilder.createUser("login@pharmacyhub.pk", 
                passwordEncoder.encode("password123"), UserType.PHARMACIST);
        user.setVerified(true);
        
        // Add role to user
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        userRepository.save(user);
        
        // Create login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmailAddress("login@pharmacyhub.pk");
        loginRequest.setPassword("password123");
        
        // Perform login request
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        // Parse response to verify JWT token
        LoggedInUserDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                LoggedInUserDTO.class);
        
        // Verify response contains JWT token
        assertNotNull(response.getJwtToken());
        assertEquals(UserType.PHARMACIST, response.getUserType());
    }

    @Test
    void testVerifyEmail() throws Exception {
        // Create test user with verification token
        User user = TestDataBuilder.createUser("verify@pharmacyhub.pk", 
                passwordEncoder.encode("password"), UserType.PHARMACIST);
        user.setVerificationToken("test-verification-token");
        user.setVerified(false);
        
        // Add role to user
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        userRepository.save(user);
        
        // Perform verification request
        mockMvc.perform(get("/auth/verify")
                .param("token", "test-verification-token"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://pharmacyhub.pk/verification-successful"));
        
        // Verify user is now verified
        User verifiedUser = userRepository.findByEmailAddress("verify@pharmacyhub.pk").get();
        assertTrue(verifiedUser.isVerified());
    }

    @Test
    void testInvalidLogin() throws Exception {
        // Create test user
        User user = TestDataBuilder.createUser("login@pharmacyhub.pk", 
                passwordEncoder.encode("password123"), UserType.PHARMACIST);
        user.setVerified(true);
        
        // Add role to user
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        userRepository.save(user);
        
        // Create login request with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmailAddress("login@pharmacyhub.pk");
        loginRequest.setPassword("wrongpassword");
        
        // Perform login request - should fail
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}