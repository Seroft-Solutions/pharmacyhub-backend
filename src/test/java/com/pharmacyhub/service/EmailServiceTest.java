package com.pharmacyhub.service;

import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.entity.Otp;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.enums.UserType;
import com.pharmacyhub.util.TestDataBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest extends BaseIntegrationTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void testSendVerificationEmail() throws MessagingException {
        // Setup
        String email = "test@pharmacyhub.pk";
        String token = "test-verification-token";
        
        // Create mock MimeMessage
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);
        
        // Send verification email
        emailService.sendVerificationEmail(email, token);
        
        // Verify that the email was sent with the correct parameters
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendHtmlMail() throws MessagingException {
        // Setup
        User user = TestDataBuilder.createUser("test@pharmacyhub.pk", "password", UserType.PHARMACIST);
        
        Otp otp = new Otp();
        otp.setUser(user);
        otp.setCode("123456");
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        
        // Create mock MimeMessage
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);
        
        // Send OTP email
        emailService.sendHtmlMail(otp);
        
        // Verify that the email was sent with the correct parameters
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testPrepareHtmlContent() {
        // Prepare test data
        String template = "Test ${placeholder} template";
        String expectedResult = "Test value template";
        
        // Mock loadHtmlTemplate method to return the test template
        EmailService spyEmailService = spy(emailService);
        doReturn(template).when(spyEmailService).loadHtmlTemplate(anyString());
        
        // Test prepareHtmlContent method
        String result = spyEmailService.prepareHtmlContent("${placeholder}", "value", "template.html");
        
        // Verify result
        assertEquals(expectedResult, result);
    }
}
