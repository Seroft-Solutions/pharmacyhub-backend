package com.pharmacyhub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class HealthService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;
        String message = "API server is available";

        // Check database connectivity
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            details.put("database", "connected");
        } catch (Exception e) {
            isHealthy = false;
            message = "Database connection failed";
            details.put("database", "disconnected");
            details.put("error", e.getMessage());
        }

        // Add system info
        details.put("timestamp", LocalDateTime.now().toString());
        details.put("memory", Runtime.getRuntime().freeMemory() / 1024 / 1024 + "MB free");
        details.put("version", "1.0.0");
        details.put("environment", System.getProperty("spring.profiles.active", "default"));

        status.put("status", isHealthy);
        status.put("message", message);
        status.put("details", details);

        return status;
    }
}
