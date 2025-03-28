package com.pharmacyhub.controller;

import com.pharmacyhub.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
public class DiagnosticController {
    private static final Logger logger = LoggerFactory.getLogger(DiagnosticController.class);

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private Environment environment;

    @GetMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestParam String email) {
        logger.info("Received email test request for: {}", email);
        try {
            String result = emailService.sendTestEmail(email);
            logger.info("Email test result: {}", result);
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", result
            ));
        } catch (Exception e) {
            logger.error("Email test failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Email test failed: " + e.getMessage(),
                "error", e.getClass().getName()
            ));
        }
    }
    
    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("mail.host", environment.getProperty("spring.mail.host", "<not set>"));
        config.put("mail.port", environment.getProperty("spring.mail.port", "<not set>"));
        config.put("mail.username", environment.getProperty("spring.mail.username", "<not set>"));
        config.put("mail.auth", environment.getProperty("spring.mail.properties.mail.smtp.auth", "<not set>"));
        config.put("mail.starttls", environment.getProperty("spring.mail.properties.mail.smtp.starttls.enable", "<not set>"));
        config.put("frontend.url", environment.getProperty("pharmacyhub.frontend.url", "<not set>"));
        config.put("google.oauth.redirect", environment.getProperty("google.oauth.redirect-uri", "<not set>"));
        
        // Include application environment information
        config.put("active.profiles", String.join(", ", environment.getActiveProfiles()));
        config.put("server.port", environment.getProperty("server.port", "8080"));
        
        logger.info("Config diagnostic requested");
        return ResponseEntity.ok(config);
    }
}
