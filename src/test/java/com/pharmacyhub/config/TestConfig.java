package com.pharmacyhub.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;

@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class TestConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public AuthenticationManager testAuthenticationManager() {
        return authentication -> {
            Set<SimpleGrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_ROLE_ASSIGN"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_GROUP_ASSIGN"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_PERMISSION_MANAGE"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_USER_READ"));
            
            Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin", "password", authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            return auth;
        };
    }
}
