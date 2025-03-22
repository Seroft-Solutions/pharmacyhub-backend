package com.pharmacyhub.payment.manual.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for storing manual payment requests.
 * Used for JazzCash and other manual payment methods where offline verification is required.
 */
@Entity
@Table(name = "payment_manual_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentManualRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private Long examId;
    
    @Column(nullable = false)
    private String senderNumber;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = true, length = 2000)
    private String notes;
    
    @Column(nullable = true)
    private String attachmentUrl;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String screenshotData;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime processedAt;
    
    @Column(nullable = true, length = 2000)
    private String adminNotes;
    
    /**
     * Payment status enum.
     * PENDING - Initially submitted, waiting for admin verification
     * APPROVED - Admin has verified and approved the payment
     * REJECTED - Admin has rejected the payment
     */
    public enum PaymentStatus {
        PENDING, APPROVED, REJECTED
    }
}