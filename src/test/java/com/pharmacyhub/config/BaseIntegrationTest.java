package com.pharmacyhub.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import com.pharmacyhub.repository.UserRepository;
import com.pharmacyhub.security.CustomUserDetailsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected CustomUserDetailsService customUserDetailsService;
    
    /**
     * Clear security context after each test
     */
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
