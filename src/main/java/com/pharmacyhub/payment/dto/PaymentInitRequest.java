package com.pharmacyhub.payment.dto;

import com.pharmacyhub.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitRequest {
    private Payment.PaymentMethod paymentMethod;
}