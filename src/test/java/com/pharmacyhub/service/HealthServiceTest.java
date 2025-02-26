package com.pharmacyhub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class HealthServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HealthService healthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getHealthStatus_WhenDatabaseIsConnected_ReturnsHealthyStatus() {
        // Arrange
        when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class)))
            .thenReturn(1);

        // Act
        Map<String, Object> healthStatus = healthService.getHealthStatus();

        // Assert
        assertTrue((Boolean) healthStatus.get("status"));
        assertEquals("API server is available", healthStatus.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) healthStatus.get("details");
        assertEquals("connected", details.get("database"));
        assertNotNull(details.get("timestamp"));
        assertNotNull(details.get("memory"));
        assertNotNull(details.get("version"));
        assertNotNull(details.get("environment"));
    }

    @Test
    void getHealthStatus_WhenDatabaseIsDisconnected_ReturnsUnhealthyStatus() {
        // Arrange
        when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        Map<String, Object> healthStatus = healthService.getHealthStatus();

        // Assert
        assertFalse((Boolean) healthStatus.get("status"));
        assertEquals("Database connection failed", healthStatus.get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) healthStatus.get("details");
        assertEquals("disconnected", details.get("database"));
        assertNotNull(details.get("error"));
    }
}
