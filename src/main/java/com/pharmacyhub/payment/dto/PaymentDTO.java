package com.pharmacyhub.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private BigDecimal amount;
    private String status;
    private String method;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String itemType;
    private Long itemId;
    private String itemName; // Added for display purposes
}