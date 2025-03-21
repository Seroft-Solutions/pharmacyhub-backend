package com.pharmacyhub.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetails {
    private Long paymentId;
    private BigDecimal amount;
    private String currency = "PKR";
    private String transactionId;
    private String gatewayUrl;
    private Map<String, String> formParameters = new HashMap<>(); 
    private String itemName;
}