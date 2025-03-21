package com.pharmacyhub.payment.controller;

import com.pharmacyhub.dto.ExamDTO;
import com.pharmacyhub.payment.dto.PaymentDTO;
import com.pharmacyhub.payment.dto.PaymentDetails;
import com.pharmacyhub.payment.dto.PaymentInitRequest;
import com.pharmacyhub.payment.dto.PaymentInitResponse;
import com.pharmacyhub.payment.dto.PaymentResult;
import com.pharmacyhub.payment.entity.Payment;
import com.pharmacyhub.payment.service.PaymentService;
import com.pharmacyhub.service.ExamService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    private final ExamService examService;
    
    @Autowired
    public PaymentController(PaymentService paymentService, ExamService examService) {
        this.paymentService = paymentService;
        this.examService = examService;
    }
    
    /**
     * Initiate a payment for an exam
     */
    @PostMapping("/exams/{examId}/pay")
    public ResponseEntity<PaymentInitResponse> initiateExamPayment(
            @PathVariable Long examId,
            @RequestBody PaymentInitRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest servletRequest) {
        
        String userId = userDetails.getUsername();
        String baseUrl = getBaseUrl(servletRequest);
        String callbackUrl = baseUrl + "/api/v1/payments/callback";
        
        PaymentInitResponse response = paymentService.initializeExamPayment(
            examId,
            userId,
            request.getPaymentMethod(),
            callbackUrl
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Payment callback endpoint
     */
    @PostMapping("/callback")
    public ResponseEntity<String> paymentCallback(@RequestParam Map<String, String> params) {
        log.info("Payment callback received: {}", params);
        
        String transactionId = params.get("pp_TxnRefNo");
        String status = params.get("pp_ResponseCode");
        
        if (transactionId == null) {
            return ResponseEntity.badRequest().body("Missing transaction ID");
        }
        
        PaymentResult result = paymentService.processPaymentCallback(transactionId, status, params);
        
        if (result.isSuccess()) {
            // Successful payment - redirect to success page
            return ResponseEntity.ok("Payment processed successfully");
        } else {
            // Failed payment - redirect to failure page
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body("Payment failed: " + result.getMessage());
        }
    }
    
    /**
     * Get payment details by transaction ID
     */
    @GetMapping("/details/{transactionId}")
    public ResponseEntity<PaymentDetails> getPaymentDetails(
            @PathVariable String transactionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        PaymentDetails details = paymentService.getPaymentDetailsByTransaction(transactionId);
        return ResponseEntity.ok(details);
    }
    
    /**
     * Check if user has access to a premium exam
     */
    @GetMapping("/exams/{examId}/access")
    public ResponseEntity<Map<String, Boolean>> checkExamAccess(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = userDetails.getUsername();
        
        boolean hasAccess = paymentService.hasUserPurchasedExam(examId, userId);
        
        Map<String, Boolean> response = Collections.singletonMap("hasAccess", hasAccess);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get payment history for current user
     */
    @GetMapping("/history")
    public ResponseEntity<List<PaymentDTO>> getPaymentHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = userDetails.getUsername();
        
        List<Payment> payments = paymentService.getUserPaymentHistory(userId);
        List<PaymentDTO> paymentDTOs = payments.stream()
            .map(this::mapToPaymentDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(paymentDTOs);
    }
    
    /**
     * Get base URL from request
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        // Build base URL
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        // Add port if not default
        if ((scheme.equals("http") && serverPort != 80)
                || (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        
        url.append(contextPath);
        return url.toString();
    }
    
    /**
     * Map Payment entity to DTO
     */
    private PaymentDTO mapToPaymentDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().toString());
        dto.setMethod(payment.getMethod().toString());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setCompletedAt(payment.getCompletedAt());
        dto.setItemType(payment.getItemType());
        dto.setItemId(payment.getItemId());
        
        // Set item name if it's an exam (in a real implementation, we'd look up other item types too)
        if ("EXAM".equals(payment.getItemType())) {
            try {
                String examTitle = examService.findById(payment.getItemId())
                    .map(exam -> exam.getTitle())
                    .orElse("Unknown Exam");
                dto.setItemName(examTitle);
            } catch (Exception e) {
                dto.setItemName("Unknown Exam");
            }
        }
        
        return dto;
    }
}