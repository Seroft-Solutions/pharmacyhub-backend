package com.pharmacyhub.controller;

import com.pharmacyhub.service.HealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class HealthControllerTest {

    @Mock
    private HealthService healthService;

    @InjectMocks
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkHealth_WhenHealthy_ReturnsOk() {
        // Arrange
        Map<String, Object> healthyResponse = new HashMap<>();
        healthyResponse.put("status", true);
        healthyResponse.put("message", "API server is available");
        when(healthService.getHealthStatus()).thenReturn(healthyResponse);

        // Act
        ResponseEntity<Map<String, Object>> response = healthController.checkHealth();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("status"));
    }

    @Test
    void checkHealth_WhenUnhealthy_ReturnsServiceUnavailable() {
        // Arrange
        Map<String, Object> unhealthyResponse = new HashMap<>();
        unhealthyResponse.put("status", false);
        unhealthyResponse.put("message", "Database connection failed");
        when(healthService.getHealthStatus()).thenReturn(unhealthyResponse);

        // Act
        ResponseEntity<Map<String, Object>> response = healthController.checkHealth();

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("status"));
        assertEquals("Database connection failed", response.getBody().get("message"));
    }
}
