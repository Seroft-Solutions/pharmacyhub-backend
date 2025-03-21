package com.pharmacyhub.payment.gateway;

import com.pharmacyhub.payment.dto.PaymentGatewayResponse;
import com.pharmacyhub.payment.dto.PaymentWebhookResult;
import com.pharmacyhub.payment.entity.Payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Interface for payment gateway integration
 */
public interface PaymentGatewayService {
    /**
     * Initialize a payment request with the payment gateway
     * @param amount Amount to be charged
     * @param description Payment description
     * @param userId User ID making the payment
     * @param callbackUrl URL to redirect after payment
     * @return Gateway response with payment URL and transaction ID
     */
    PaymentGatewayResponse initializePayment(BigDecimal amount, String description, String userId, String callbackUrl);
    
    /**
     * Check payment status with the payment gateway
     * @param transactionId Transaction ID from the payment gateway
     * @return Payment status from the gateway
     */
    PaymentStatus checkPaymentStatus(String transactionId);
    
    /**
     * Process webhook notification from payment gateway
     * @param requestData Webhook request data
     * @return Processed payment result
     */
    PaymentWebhookResult processWebhookNotification(Map<String, String> requestData);
}