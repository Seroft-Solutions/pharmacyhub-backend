package com.pharmacyhub.controller;

import com.pharmacyhub.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private HealthService healthService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> healthStatus = healthService.getHealthStatus();
        boolean isHealthy = (boolean) healthStatus.get("status");
        
        return ResponseEntity
            .status(isHealthy ? 200 : 503)
            .body(healthStatus);
    }
}
