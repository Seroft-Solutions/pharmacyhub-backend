package com.pharmacyhub.payment.controller;

import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.payment.entity.Payment;
import com.pharmacyhub.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Development-only controller for simulating payments
 * This controller is only active in the "dev" profile
 */
@RestController
@RequestMapping("/api/v1/dev/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
@Profile({"dev", "local"}) // Only available in development profiles
@Slf4j
public class DevPaymentController {

    private final PaymentRepository paymentRepository;

    @Autowired
    public DevPaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Simulate a successful payment for testing the "pay once, access all" feature
     */
    @PostMapping("/simulate-success")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateSuccessfulPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "examId", defaultValue = "1") Long examId) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "User details not found"));
        }
        
        String userId = userDetails.getUsername();
        log.info("Simulating successful payment for user: {} and examId: {}", userId, examId);
        
        // Create a successful payment record
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setItemId(examId);
        payment.setItemType("EXAM");
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setMethod(Payment.PaymentMethod.JAZZCASH);
        payment.setTransactionId("DEV-" + System.currentTimeMillis());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setCompletedAt(LocalDateTime.now());
        payment.setPaymentResponse("{\"simulated\": true}");
        
        Payment savedPayment = paymentRepository.save(payment);
        
        Map<String, Object> response = Collections.singletonMap(
            "success", true
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}