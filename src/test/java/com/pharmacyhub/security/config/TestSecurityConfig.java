package com.pharmacyhub.security.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import java.io.Serializable;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").permitAll()
            )
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            
        return http.build();
    }
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/**");
    }

    @Bean
    @Primary
    public MethodSecurityExpressionHandler expressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator());
        return handler;
    }

    @Bean
    @Primary
    public PermissionEvaluator permissionEvaluator() {
        return new PermissionEvaluator() {
            @Override
            public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
                return true; // Allow all permissions in tests
            }

            @Override
            public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
                return true; // Allow all permissions in tests
            }
        };
    }
}
