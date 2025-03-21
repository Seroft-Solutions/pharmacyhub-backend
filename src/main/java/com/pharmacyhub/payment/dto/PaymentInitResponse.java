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
public class PaymentInitResponse {
    private Long paymentId;
    private String transactionId;
    private String redirectUrl;
    private Map<String, String> formParameters = new HashMap<>(); 
}