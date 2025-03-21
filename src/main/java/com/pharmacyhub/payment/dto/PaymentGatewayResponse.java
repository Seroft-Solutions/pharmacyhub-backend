package com.pharmacyhub.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResponse {
    private String transactionId;
    private String paymentUrl;
    private String status;
    private Map<String, String> additionalData = new HashMap<>();
}