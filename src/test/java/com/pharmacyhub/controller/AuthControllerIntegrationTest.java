package com.pharmacyhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacyhub.config.TestConfig;
import com.pharmacyhub.dto.LoggedInUserDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.security.model.LoginRequest;
import com.pharmacyhub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.pharmacyhub.PharmacyHubApplication;
import com.pharmacyhub.config.SecurityConfig;
import com.pharmacyhub.config.MyConfig;
import com.pharmacyhub.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {PharmacyHubApplication.class})
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@ComponentScan(basePackages = "com.pharmacyhub", 
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, 
        classes = {SecurityConfig.class, MyConfig.class}
    )
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void whenValidSignup_thenReturnsSuccess() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmailAddress("test@example.com");
        userDTO.setPassword("Test@123");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully. Please check your email for verification."));
    }

    @Test
    void whenDuplicateEmail_thenReturnsConflict() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmailAddress("duplicate@example.com");
        userDTO.setPassword("Test@123");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");

        // First registration
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk());

        // Duplicate registration
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenValidVerification_thenRedirectsToSuccess() throws Exception {
        // Create and save a user
        UserDTO userDTO = new UserDTO();
        userDTO.setEmailAddress("verify@example.com");
        userDTO.setPassword("Test@123");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        
        userService.saveUser(userDTO);

        // Get verification token (in real scenario this would be sent via email)
        User user = userService.getUserByEmailAddress(userDTO);
        String token = user.getVerificationToken();

        mockMvc.perform(get("/api/auth/verify")
                .param("token", token))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://pharmacyhub.pk/verification-successful"));
    }

    @Test
    void whenInvalidVerification_thenRedirectsToFailure() throws Exception {
        mockMvc.perform(get("/api/auth/verify")
                .param("token", "invalid-token"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://pharmacyhub.pk/verification-failed"));
    }

    @Test
    void whenValidLogin_thenReturnsToken() throws Exception {
        // Create and verify a user first
        UserDTO userDTO = new UserDTO();
        userDTO.setEmailAddress("login@example.com");
        userDTO.setPassword("Test@123");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        
        userService.saveUser(userDTO);
        User user = userService.getUserByEmailAddress(userDTO);
        userService.verifyUser(user.getVerificationToken());

        // Attempt login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmailAddress("login@example.com");
        loginRequest.setPassword("Test@123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoggedInUserDTO response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            LoggedInUserDTO.class
        );

        assertNotNull(response.getJwtToken());
    }

    @Test
    void whenInvalidLogin_thenReturnsBadCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmailAddress("invalid@example.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Credentials Invalid !!"));
    }
}
