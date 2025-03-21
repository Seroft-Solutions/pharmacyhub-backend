package com.pharmacyhub.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookResult {
    private String transactionId;
    private String userId;
    private String status;
    private String message;
    private String itemType;
    private Long itemId;
}