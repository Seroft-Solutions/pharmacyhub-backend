package com.pharmacyhub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing security tokens (verification, password reset, etc.)
 */
@Entity
@Table(name = "security_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 512)
    private String token;
    
    @Column(nullable = true)
    private Long userId;
    
    @Column(nullable = false, length = 100)
    private String purpose;
    
    @Column(nullable = false)
    private LocalDateTime expirationTime;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Check if token is expired
     * 
     * @return true if token is expired
     */
    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }
}
