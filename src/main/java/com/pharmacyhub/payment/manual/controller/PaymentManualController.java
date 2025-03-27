package com.pharmacyhub.payment.manual.controller;

import com.pharmacyhub.payment.manual.dto.ManualPaymentProcessDTO;
import com.pharmacyhub.payment.manual.dto.ManualPaymentResponseDTO;
import com.pharmacyhub.payment.manual.dto.ManualPaymentSubmitDTO;
import com.pharmacyhub.payment.manual.dto.PaymentStatisticsDTO;
import com.pharmacyhub.payment.manual.entity.PaymentManualRequest;
import com.pharmacyhub.payment.manual.service.PaymentManualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for manual payment operations
 */
@RestController
@RequestMapping("/api/v1/payments/manual")
@RequiredArgsConstructor
@Slf4j
public class PaymentManualController {
    
    private final PaymentManualService paymentManualService;
    
    /**
     * Submit a manual payment request
     */
    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ManualPaymentResponseDTO> submitRequest(
            @Valid @RequestBody ManualPaymentSubmitDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = userDetails.getUsername();
        ManualPaymentResponseDTO response = paymentManualService.submitRequest(userId, request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user's own payment requests
     */
    @GetMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ManualPaymentResponseDTO>> getUserRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String includeScreenshots) {
        
        String userId = userDetails.getUsername();
        boolean includeImageData = "true".equalsIgnoreCase(includeScreenshots);
        
        List<ManualPaymentResponseDTO> requests = paymentManualService.getUserRequests(userId, includeImageData);
        
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Check if user has access to an exam via manual payment
     */
    @GetMapping("/exams/{examId}/access")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkExamAccess(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = userDetails.getUsername();
        boolean hasManualAccess = paymentManualService.hasUserApprovedRequest(userId, examId);
        
        Map<String, Boolean> response = Collections.singletonMap("hasAccess", hasManualAccess);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user has pending request for an exam
     */
    @GetMapping("/exams/{examId}/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkPendingRequest(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = userDetails.getUsername();
        boolean hasPending = paymentManualService.hasUserPendingRequest(userId, examId);
        
        Map<String, Boolean> response = Collections.singletonMap("hasPending", hasPending);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Admin endpoints below
     */
    
    /**
     * Get all payment requests (admin)
     */
    @GetMapping("/admin/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ManualPaymentResponseDTO>> getAllRequests() {
        List<ManualPaymentResponseDTO> requests = paymentManualService.getAllRequests();
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get payment requests by status (admin)
     */
    @GetMapping("/admin/requests/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ManualPaymentResponseDTO>> getRequestsByStatus(
            @PathVariable String status) {
        
        PaymentManualRequest.PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentManualRequest.PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment status: {}", status);
            return ResponseEntity.badRequest().build();
        }
        
        List<ManualPaymentResponseDTO> requests = paymentManualService.getRequestsByStatus(paymentStatus);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Approve a payment request (admin)
     */
    @PostMapping("/admin/requests/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManualPaymentResponseDTO> approveRequest(
            @PathVariable Long requestId,
            @RequestBody ManualPaymentProcessDTO processDTO) {
        
        ManualPaymentResponseDTO response = paymentManualService.approveRequest(requestId, processDTO);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reject a payment request (admin)
     */
    @PostMapping("/admin/requests/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManualPaymentResponseDTO> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody ManualPaymentProcessDTO processDTO) {
        
        ManualPaymentResponseDTO response = paymentManualService.rejectRequest(requestId, processDTO);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get payment statistics (admin)
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentStatisticsDTO> getPaymentStatistics() {
        PaymentStatisticsDTO statistics = paymentManualService.getPaymentStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get payment history summary (admin)
     */
    @GetMapping("/admin/history/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentHistorySummary() {
        Map<String, Object> summary = paymentManualService.getPaymentHistorySummary();
        return ResponseEntity.ok(summary);
    }
}