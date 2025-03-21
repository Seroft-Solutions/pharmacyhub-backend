package com.pharmacyhub.payment.gateway;

import com.pharmacyhub.payment.config.JazzCashProperties;
import com.pharmacyhub.payment.dto.PaymentGatewayResponse;
import com.pharmacyhub.payment.dto.PaymentWebhookResult;
import com.pharmacyhub.payment.entity.Payment.PaymentStatus;
import com.pharmacyhub.payment.exception.PaymentGatewayException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JazzCashPaymentGatewayService implements PaymentGatewayService {
    
    private final JazzCashProperties jazzCashProperties;
    private final RestTemplate restTemplate;
    
    @Autowired
    public JazzCashPaymentGatewayService(JazzCashProperties jazzCashProperties, RestTemplate restTemplate) {
        this.jazzCashProperties = jazzCashProperties;
        this.restTemplate = restTemplate;
    }
    
    @Override
    public PaymentGatewayResponse initializePayment(BigDecimal amount, String description, String userId, String callbackUrl) {
        try {
            String txnDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String txnRefNo = "TXN" + txnDateTime;
            
            // Round to 2 decimals and convert to string without decimal point
            String amountStr = amount.setScale(2, RoundingMode.HALF_UP).toString().replace(".", "");
            
            Map<String, String> request = new HashMap<>();
            request.put("pp_MerchantID", jazzCashProperties.getMerchantId());
            request.put("pp_Password", jazzCashProperties.getPassword());
            request.put("pp_Amount", amountStr);
            request.put("pp_TxnRefNo", txnRefNo);
            request.put("pp_TxnDateTime", txnDateTime);
            request.put("pp_BillReference", "EXAM" + System.currentTimeMillis());
            request.put("pp_Description", description);
            request.put("pp_Language", "EN");
            request.put("pp_ReturnURL", callbackUrl);
            request.put("pp_TxnCurrency", "PKR");
            request.put("pp_TxnType", "MWALLET");
            request.put("ppmpf_1", userId);
            
            // Generate secure hash
            String secureHash = generateSecureHash(request);
            request.put("pp_SecureHash", secureHash);
            
            // For JazzCash, we may need to render a form that auto-submits to their gateway
            // instead of making a direct API call
            
            PaymentGatewayResponse response = new PaymentGatewayResponse();
            response.setTransactionId(txnRefNo);
            
            if (jazzCashProperties.isTestMode()) {
                response.setPaymentUrl(jazzCashProperties.getApiBaseUrl() + "/sandbox/");
            } else {
                response.setPaymentUrl(jazzCashProperties.getApiBaseUrl());
            }
            
            // Add request parameters to be used for form submission
            response.setAdditionalData(request);
            
            return response;
        } catch (Exception e) {
            log.error("Error initializing JazzCash payment", e);
            throw new PaymentGatewayException("Failed to initialize payment with JazzCash", e);
        }
    }
    
    @Override
    public PaymentStatus checkPaymentStatus(String transactionId) {
        // Implement logic to check payment status with JazzCash
        // This might involve calling a status API
        return PaymentStatus.PENDING;
    }
    
    @Override
    public PaymentWebhookResult processWebhookNotification(Map<String, String> requestData) {
        PaymentWebhookResult result = new PaymentWebhookResult();
        
        try {
            String txnRefNo = requestData.get("pp_TxnRefNo");
            String responseCode = requestData.get("pp_ResponseCode");
            String userId = requestData.get("ppmpf_1");
            
            result.setTransactionId(txnRefNo);
            result.setUserId(userId);
            
            // Validate the secure hash to ensure the request came from JazzCash
            String receivedHash = requestData.get("pp_SecureHash");
            String computedHash = computeSecureHashFromResponse(requestData);
            
            if (receivedHash != null && !receivedHash.equals(computedHash)) {
                result.setStatus("INVALID");
                result.setMessage("Invalid secure hash");
                return result;
            }
            
            // Check response code
            if ("000".equals(responseCode)) {
                result.setStatus("COMPLETED");
                result.setMessage("Payment completed successfully");
            } else {
                result.setStatus("FAILED");
                result.setMessage("Payment failed with code: " + responseCode);
            }
            
            // Extract item info if available
            String billRef = requestData.get("pp_BillReference");
            if (billRef != null && billRef.startsWith("EXAM")) {
                result.setItemType("EXAM");
                // Extract exam ID from bill reference if possible
                // For now, just set a placeholder
                result.setItemId(0L);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error processing JazzCash webhook", e);
            result.setStatus("ERROR");
            result.setMessage("Error processing payment notification: " + e.getMessage());
            return result;
        }
    }
    
    private String generateSecureHash(Map<String, String> request) {
        // Implementation for generating secure hash according to JazzCash documentation
        StringBuilder sb = new StringBuilder();
        sb.append(request.get("pp_Amount"))
          .append(request.get("pp_BillReference"))
          .append(request.get("pp_Description"))
          .append(request.get("pp_Language"))
          .append(request.get("pp_MerchantID"))
          .append(request.get("pp_Password"))
          .append(request.get("pp_ReturnURL"))
          .append(request.get("pp_TxnCurrency"))
          .append(request.get("pp_TxnDateTime"))
          .append(request.get("pp_TxnRefNo"))
          .append(request.get("pp_TxnType"))
          .append(request.get("ppmpf_1"));
        
        // Apply SHA-256 hashing with integrity key
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(jazzCashProperties.getIntegrityKey().getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] hash = sha256_HMAC.doFinal(sb.toString().getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secure hash", e);
        }
    }
    
    private String computeSecureHashFromResponse(Map<String, String> response) {
        // Similar implementation to verify response integrity
        // This would depend on the exact JazzCash documentation
        // For now, just return an empty string as placeholder
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