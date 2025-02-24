package com.pharmacyhub.service;

import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.dto.ChangePasswordDTO;
import com.pharmacyhub.dto.UserDTO;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.domain.Role;
import com.pharmacyhub.repository.RoleRepository;
import com.pharmacyhub.util.TestDataBuilder;
import com.pharmacyhub.util.WithMockUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    private Role userRole;

    @BeforeEach
    void setUp() throws Exception {
        // Clear data before each test
        userRepository.deleteAll();
        
        // Create user role
        userRole = TestDataBuilder.createRole(RoleEnum.USER, 5);
        userRole = roleRepository.save(userRole);
        
        // Mock email service to avoid sending emails
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());
        doNothing().when(emailService).sendHtmlMail(any());
    }

    @Test
    void testSaveUser() throws Exception {
        // Create test user DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setEmailAddress("test@pharmacyhub.pk");
        userDTO.setPassword("password123");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        
        // Save user
        UserDTO savedUserDTO = (UserDTO) userService.saveUser(userDTO);
        
        // Verify user was saved
        assertNotNull(savedUserDTO);
        
        // Verify user exists in database
        Optional<User> userOpt = userRepository.findByEmailAddress("test@pharmacyhub.pk");
        assertTrue(userOpt.isPresent());
        
        User user = userOpt.get();
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertTrue(passwordEncoder.matches("password123", user.getPassword()));
        
        // Verify verification token was generated
        assertNotNull(user.getVerificationToken());
        assertNotNull(user.getTokenCreationDate());
    }

    @Test
    @WithMockUserPrincipal(email = "test@pharmacyhub.pk")
    void testChangeUserPassword() {
        // Create and save test user
        User user = TestDataBuilder.createUser("test@pharmacyhub.pk", 
                passwordEncoder.encode("oldPassword"), UserType.PHARMACIST);
        user.setRole(userRole);
        userRepository.save(user);
        
        // Create change password DTO
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("oldPassword");
        changePasswordDTO.setNewPassword("newPassword");
        
        // Change password
        userService.changeUserPassword(changePasswordDTO);
        
        // Verify password was changed
        User updatedUser = userRepository.findByEmailAddress("test@pharmacyhub.pk").get();
        assertTrue(passwordEncoder.matches("newPassword", updatedUser.getPassword()));
    }

    @Test
    void testVerifyUser() {
        // Create test user with verification token
        User user = TestDataBuilder.createUser("test@pharmacyhub.pk", "password", UserType.PHARMACIST);
        user.setVerificationToken("test-token");
        user.setVerified(false);
        userRepository.save(user);
        
        // Verify user
        boolean result = userService.verifyUser("test-token");
        
        // Verify result
        assertTrue(result);
        
        // Verify user is now verified
        User verifiedUser = userRepository.findByEmailAddress("test@pharmacyhub.pk").get();
        assertTrue(verifiedUser.isVerified());
        assertNull(verifiedUser.getVerificationToken());
    }

    @Test
    void testUpdateUserStatus() {
        // Create test user
        User user = TestDataBuilder.createUser("test@pharmacyhub.pk", 
                passwordEncoder.encode("password"), UserType.PHARMACIST);
        user.setOpenToConnect(false);
        user = userRepository.save(user);
        
        // Set the user as the current authenticated user
        com.pharmacyhub.util.TestSecurityUtils.setSecurityContext(user);
        
        // Update user status
        Boolean result = userService.updateUserStatus();
        
        // Verify result
        assertTrue(result);
        
        // Verify user status was updated
        User updatedUser = userRepository.findByEmailAddress("test@pharmacyhub.pk").get();
        assertTrue(updatedUser.isOpenToConnect());
        
        // Clean up security context
        com.pharmacyhub.util.TestSecurityUtils.clearSecurityContext();
    }
}
