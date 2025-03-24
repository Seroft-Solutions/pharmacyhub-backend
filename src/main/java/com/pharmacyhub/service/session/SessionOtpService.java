package com.pharmacyhub.service.session;

import com.pharmacyhub.entity.Otp;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.entity.session.LoginSession;
import com.pharmacyhub.dto.session.OtpVerificationResponseDTO;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.repository.LoginSessionRepository;
import com.pharmacyhub.repository.OtpRepository;
import com.pharmacyhub.service.EmailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for OTP generation and verification for session management
 */
@Service
@RequiredArgsConstructor
public class SessionOtpService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionOtpService.class);
    
    private final UserRepository userRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    
    @Value("${pharmacyhub.security.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;
    
    @Value("${pharmacyhub.security.otp.length:6}")
    private int otpLength;
    
    /**
     * Generate OTP for a user's session verification
     * 
     * @param userId User ID
     * @return Generated OTP
     */
    @Transactional
    public String generateOtp(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        // Generate random OTP
        String code = generateRandomCode(otpLength);
        
        // Create OTP entity
        Otp otp = new Otp();
        otp.setUser(user);
        otp.setCode(code);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        
        // Save OTP to database
        otpRepository.save(otp);
        
        // In a real implementation, send OTP to user via email/SMS
        try {
            emailService.sendHtmlMail(otp);
            logger.debug("Generated and sent OTP for user {}: {}", userId, code);
        } catch (Exception e) {
            logger.error("Failed to send OTP email", e);
        }
        
        return code;
    }
    
    /**
     * Verify OTP for a session
     * 
     * @param userId User ID
     * @param sessionId Session ID
     * @param otpCode OTP code
     * @return Verification result
     */
    @Transactional
    public OtpVerificationResponseDTO verifyOtp(Long userId, UUID sessionId, String otpCode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        LoginSession session = loginSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
        
        // Check if the OTP exists and is valid
        Optional<Otp> otpOptional = otpRepository.findTopByUserAndCodeOrderByCreatedAtDesc(user, otpCode);
        
        if (otpOptional.isPresent()) {
            Otp otp = otpOptional.get();
            
            // Check if OTP is not expired
            boolean isValid = !otp.getExpiresAt().isBefore(LocalDateTime.now());
            
            if (isValid) {
                // Mark session as verified
                session.setOtpVerified(true);
                session.setRequiresOtp(false);
                loginSessionRepository.save(session);
                
                logger.info("Successfully verified OTP for user {} and session {}", userId, sessionId);
                
                return OtpVerificationResponseDTO.builder()
                    .success(true)
                    .message("OTP verified successfully")
                    .sessionId(sessionId)
                    .build();
            } else {
                logger.warn("OTP expired for user {} and session {}", userId, sessionId);
                return OtpVerificationResponseDTO.builder()
                    .success(false)
                    .message("OTP expired. Please request a new one.")
                    .build();
            }
        } else {
            logger.warn("Invalid OTP provided for user {} and session {}", userId, sessionId);
            return OtpVerificationResponseDTO.builder()
                .success(false)
                .message("Invalid OTP. Please try again.")
                .build();
        }
    }
    
    /**
     * Generate a random numeric code
     * 
     * @param length Length of the code
     * @return Random code
     */
    private String generateRandomCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
}
