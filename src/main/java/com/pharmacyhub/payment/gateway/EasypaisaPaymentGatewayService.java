package com.pharmacyhub.payment.gateway;

import com.pharmacyhub.payment.config.EasypaisaProperties;
import com.pharmacyhub.payment.dto.PaymentGatewayResponse;
import com.pharmacyhub.payment.dto.PaymentWebhookResult;
import com.pharmacyhub.payment.entity.Payment.PaymentStatus;
import com.pharmacyhub.payment.exception.PaymentGatewayException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EasypaisaPaymentGatewayService implements PaymentGatewayService {
    
    private final EasypaisaProperties easypaisaProperties;
    private final RestTemplate restTemplate;
    
    @Autowired
    public EasypaisaPaymentGatewayService(EasypaisaProperties easypaisaProperties, RestTemplate restTemplate) {
        this.easypaisaProperties = easypaisaProperties;
        this.restTemplate = restTemplate;
    }
    
    @Override
    public PaymentGatewayResponse initializePayment(BigDecimal amount, String description, String userId, String callbackUrl) {
        try {
            String orderId = "EP" + System.currentTimeMillis();
            String amountStr = amount.setScale(2, RoundingMode.HALF_UP).toString();
            
            // Create request parameters
            Map<String, String> request = new HashMap<>();
            request.put("merchantId", easypaisaProperties.getMerchantId());
            request.put("accountNumber", easypaisaProperties.getAccountNumber());
            request.put("orderId", orderId);
            request.put("amount", amountStr);
            request.put("description", description);
            request.put("callbackUrl", callbackUrl);
            request.put("customerId", userId);
            
            // Generate checksum/hash
            String checksum = generateChecksum(request);
            request.put("checksum", checksum);
            
            PaymentGatewayResponse response = new PaymentGatewayResponse();
            response.setTransactionId(orderId);
            
            if (easypaisaProperties.isTestMode()) {
                response.setPaymentUrl(easypaisaProperties.getApiBaseUrl() + "/sandbox/index.jsp");
            } else {
                response.setPaymentUrl(easypaisaProperties.getApiBaseUrl() + "/index.jsp");
            }
            
            response.setAdditionalData(request);
            
            return response;
        } catch (Exception e) {
            log.error("Error initializing Easypaisa payment", e);
            throw new PaymentGatewayException("Failed to initialize payment with Easypaisa", e);
        }
    }
    
    @Override
    public PaymentStatus checkPaymentStatus(String transactionId) {
        // Would implement a real call to Easypaisa API if available
        // For now return pending as a placeholder
        return PaymentStatus.PENDING;
    }
    
    @Override
    public PaymentWebhookResult processWebhookNotification(Map<String, String> requestData) {
        PaymentWebhookResult result = new PaymentWebhookResult();
        
        try {
            String orderId = requestData.get("orderId");
            String responseCode = requestData.get("responseCode");
            String userId = requestData.get("customerId");
            
            result.setTransactionId(orderId);
            result.setUserId(userId);
            
            // Validate the checksum to ensure the request came from Easypaisa
            String receivedChecksum = requestData.get("checksum");
            String computedChecksum = computeChecksumFromResponse(requestData);
            
            if (receivedChecksum != null && !receivedChecksum.equals(computedChecksum)) {
                result.setStatus("INVALID");
                result.setMessage("Invalid checksum");
                return result;
            }
            
            // Check response code
            if ("0000".equals(responseCode)) {
                result.setStatus("COMPLETED");
                result.setMessage("Payment completed successfully");
            } else {
                result.setStatus("FAILED");
                result.setMessage("Payment failed with code: " + responseCode);
            }
            
            // Set default item type - would parse from actual data in production
            result.setItemType("EXAM");
            result.setItemId(0L); // Placeholder
            
            return result;
        } catch (Exception e) {
            log.error("Error processing Easypaisa webhook", e);
            result.setStatus("ERROR");
            result.setMessage("Error processing payment notification: " + e.getMessage());
            return result;
        }
    }
    
    private String generateChecksum(Map<String, String> request) {
        // This is a placeholder implementation - actual implementation would follow Easypaisa docs
        try {
            String dataToHash = 
                request.get("merchantId") +
                request.get("accountNumber") +
                request.get("orderId") +
                request.get("amount") +
                easypaisaProperties.getSecretKey();
                
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate checksum", e);
        }
    }
    
    private String computeChecksumFromResponse(Map<String, String> response) {
        // Placeholder implementation - actual would follow Easypaisa docs
        return "";
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}