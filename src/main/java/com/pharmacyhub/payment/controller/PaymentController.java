package com.pharmacyhub.payment.controller;

import com.pharmacyhub.dto.ExamDTO;
import com.pharmacyhub.payment.dto.PaymentDTO;
import com.pharmacyhub.payment.dto.PaymentDetails;
import com.pharmacyhub.payment.dto.PaymentInitRequest;
import com.pharmacyhub.payment.dto.PaymentInitResponse;
import com.pharmacyhub.payment.dto.PaymentResult;
import com.pharmacyhub.payment.entity.Payment;
import com.pharmacyhub.payment.service.PaymentService;
import com.pharmacyhub.payment.manual.service.PaymentManualService;
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
    private final PaymentManualService paymentManualService;
    private final ExamService examService;
    
    @Autowired
    public PaymentController(PaymentService paymentService, PaymentManualService paymentManualService, ExamService examService) {
        this.paymentService = paymentService;
        this.paymentManualService = paymentManualService;
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
    public ResponseEntity<Map<String, Object>> checkExamAccess(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = userDetails.getUsername();
        
        // COMPREHENSIVE APPROACH: CHECK ALL PAYMENT METHODS
        
        // 1. Check online payments
        boolean hasPurchasedThisExam = paymentService.hasUserPurchasedExam(examId, userId);
        boolean hasPurchasedAnyExam = paymentService.hasUserPurchasedAnyExam(userId);
        
        // 2. Check manual payments
        boolean hasApprovedThisExamManualPayment = paymentManualService.hasUserApprovedRequest(userId, examId);
        boolean hasApprovedAnyManualPayment = paymentManualService.hasUserApprovedRequest(userId, null);
        
        // Determine overall access status
        boolean hasDirectAccess = hasPurchasedThisExam || hasApprovedThisExamManualPayment;
        boolean hasUniversalAccess = hasPurchasedAnyExam || hasApprovedAnyManualPayment;
        boolean hasAccess = hasDirectAccess || hasUniversalAccess;
        
        // Return detailed response with all access information
        Map<String, Object> response = Map.of(
            "hasAccess", hasAccess,
            "hasDirectAccess", hasDirectAccess,
            "hasUniversalAccess", hasUniversalAccess,
            "hasPurchasedThisExam", hasPurchasedThisExam,
            "hasPurchasedAnyExam", hasPurchasedAnyExam,
            "hasApprovedThisExamManualPayment", hasApprovedThisExamManualPayment,
            "hasApprovedAnyManualPayment", hasApprovedAnyManualPayment
        );
        
        // Log for troubleshooting
        log.info("Access check for user {} on exam {}: direct={}, universal={}, access={}",
                userId, examId, hasDirectAccess, hasUniversalAccess, hasAccess);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user has universal premium access ("pay once, access all" feature)
     */
    @GetMapping("/premium/access")
    public ResponseEntity<Map<String, Object>> checkUniversalAccess(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = userDetails.getUsername();
        
        // Check if user has purchased any premium exam (online)
        boolean hasPurchasedAnyExam = paymentService.hasUserPurchasedAnyExam(userId);
        
        // Check if user has any approved manual payment request
        boolean hasApprovedAnyManualPayment = paymentManualService.hasUserApprovedRequest(userId, null);
        
        // Combined universal access flag
        boolean hasUniversalAccess = hasPurchasedAnyExam || hasApprovedAnyManualPayment;
        
        Map<String, Object> response = Map.of(
            "hasUniversalAccess", hasUniversalAccess,
            "hasPurchasedAnyExam", hasPurchasedAnyExam,
            "hasApprovedAnyManualPayment", hasApprovedAnyManualPayment
        );
        
        // Log for troubleshooting
        log.info("Universal access check for user {}: online={}, manual={}, combined={}",
                userId, hasPurchasedAnyExam, hasApprovedAnyManualPayment, hasUniversalAccess);
        
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