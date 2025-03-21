package com.pharmacyhub.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime completedAt;
    
    @Column(name = "item_type", nullable = false)
    private String itemType;  // e.g., "EXAM"
    
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    
    @Column(nullable = true, length = 255)
    private String transactionId;  // ID from payment gateway
    
    @Column(nullable = true, length = 2000)
    private String paymentResponse;  // Response from payment gateway
    
    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED, EXPIRED
    }
    
    public enum PaymentMethod {
        CREDIT_CARD, EASYPAISA, JAZZCASH, BANK_TRANSFER
    }
}