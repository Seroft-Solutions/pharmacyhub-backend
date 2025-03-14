package com.pharmacyhub.security.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to store security audit logs
 */
@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String action;
    
    @Column(length = 1000)
    private String details;
    
    @Column(nullable = false)
    private String outcome;
    
    private String username;
    
    private Long userId;
    
    private String ipAddress;
    
    private String userAgent;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
