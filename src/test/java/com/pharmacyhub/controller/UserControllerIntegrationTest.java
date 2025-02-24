package com.pharmacyhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.dto.ChangePasswordDTO;
import com.pharmacyhub.dto.PHUserDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.repository.RoleRepository;
import com.pharmacyhub.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class UserControllerIntegrationTest extends BaseIntegrationTest {

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

    private Role userRole;
    private Role adminRole;
    private User normalUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Clear user repository
        userRepository.deleteAll();
        
        // Create roles
        if (roleRepository.findByName(RoleEnum.USER).isEmpty()) {
            userRole = TestDataBuilder.createRole(RoleEnum.USER, 5);
            userRole = roleRepository.save(userRole);
        } else {
            userRole = roleRepository.findByName(RoleEnum.USER).get();
        }
        
        if (roleRepository.findByName(RoleEnum.ADMIN).isEmpty()) {
            adminRole = TestDataBuilder.createRole(RoleEnum.ADMIN, 1);
            adminRole = roleRepository.save(adminRole);
        } else {
            adminRole = roleRepository.findByName(RoleEnum.ADMIN).get();
        }
        
        // Create normal user
        normalUser = TestDataBuilder.createUser("user@pharmacyhub.pk", 
                passwordEncoder.encode("password"), UserType.PHARMACIST);
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        normalUser.setRoles(userRoles);
        normalUser = userRepository.save(normalUser);
        
        // Create admin user
        adminUser = TestDataBuilder.createUser("admin@pharmacyhub.pk", 
                passwordEncoder.encode("password"), UserType.ADMIN);
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRoles(adminRoles);
        adminUser = userRepository.save(adminUser);
    }

    @Test
    @WithMockUser(username = "user@pharmacyhub.pk")
    void testGetUserData() throws Exception {
        // Get user data
        MvcResult result = mockMvc.perform(get("/api/v1/user-data"))
                .andExpect(status().isOk())
                .andReturn();
        
        // Parse response
        UserType userType = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                UserType.class);
        
        // Verify response
        assertEquals(UserType.PHARMACIST, userType);
    }

    @Test
    @WithMockUser(username = "user@pharmacyhub.pk")
    void testIsUserRegistered() throws Exception {
        // Check if user is registered
        MvcResult result = mockMvc.perform(get("/api/v1/is-user-registered"))
                .andExpect(status().isOk())
                .andReturn();
        
        // Parse response
        Boolean isRegistered = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                Boolean.class);
        
        // Verify response
        assertTrue(isRegistered);
    }

    @Test
    @WithMockUser(username = "user@pharmacyhub.pk")
    void testToggleUserStatus() throws Exception {
        // Toggle user status
        MvcResult result = mockMvc.perform(get("/api/v1/toggle-user-status"))
                .andExpect(status().isOk())
                .andReturn();
        
        // Parse response
        Boolean newStatus = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                Boolean.class);
        
        // Verify response
        assertTrue(newStatus);
        
        // Verify user status was updated
        User updatedUser = userRepository.findById(normalUser.getId()).get();
        assertTrue(updatedUser.isOpenToConnect());
    }

    @Test
    @WithMockUser(username = "user@pharmacyhub.pk")
    void testChangeUserPassword() throws Exception {
        // Create change password DTO
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("password");
        changePasswordDTO.setNewPassword("newPassword");
        
        // Change user password
        mockMvc.perform(put("/api/v1/change-user-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isOk());
        
        // Verify password was changed
        User updatedUser = userRepository.findById(normalUser.getId()).get();
        assertTrue(passwordEncoder.matches("newPassword", updatedUser.getPassword()));
    }

    @Test
    @WithMockUser(username = "user@pharmacyhub.pk")
    void testUpdateUserInfo() throws Exception {
        // Create user DTO for update
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Updated");
        userDTO.setLastName("User");
        
        // Update user info
        mockMvc.perform(put("/api/v1/update-user-info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk());
        
        // Verify user info was updated
        User updatedUser = userRepository.findById(normalUser.getId()).get();
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("User", updatedUser.getLastName());
    }

    @Test
    @WithMockUser(username = "admin@pharmacyhub.pk", roles = {"ADMIN"})
    void testGetAllUsers() throws Exception {
        // Get all users - admin only
        MvcResult result = mockMvc.perform(get("/api/v1/get-all-users"))
                .andExpect(status().isOk())
                .andReturn();
        
        // Verify response contains both users
        assertTrue(result.getResponse().getContentAsString().contains("user@pharmacyhub.pk"));
        assertTrue(result.getResponse().getContentAsString().contains("admin@pharmacyhub.pk"));
    }

    @Test
    @WithMockUser(username = "user@pharmacyhub.pk")
    void testGetUser() throws Exception {
        // Get user information
        mockMvc.perform(get("/api/v1/get-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress").value("user@pharmacyhub.pk"))
                .andExpect(jsonPath("$.firstName").value(normalUser.getFirstName()));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        // Attempt to access protected endpoint without authentication
        mockMvc.perform(get("/api/v1/get-user"))
                .andExpect(status().isUnauthorized());
    }
}
