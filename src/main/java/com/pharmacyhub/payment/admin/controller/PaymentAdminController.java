package com.pharmacyhub.payment.admin.controller;

import com.pharmacyhub.payment.manual.dto.ManualPaymentResponseDTO;
import com.pharmacyhub.payment.manual.dto.PaymentStatisticsDTO;
import com.pharmacyhub.payment.manual.entity.PaymentManualRequest;
import com.pharmacyhub.payment.manual.service.PaymentManualService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for admin payment operations and dashboards
 */
@RestController
@RequestMapping("/api/v1/payments/admin")
@RequiredArgsConstructor
@Slf4j
public class PaymentAdminController {
    
    private final PaymentManualService paymentManualService;
    
    /**
     * Get payment dashboard statistics
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentStatisticsDTO> getDashboardStatistics() {
        PaymentStatisticsDTO statistics = paymentManualService.getPaymentStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get payment history - all statuses
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ManualPaymentResponseDTO>> getPaymentHistory() {
        List<ManualPaymentResponseDTO> history = paymentManualService.getAllRequests();
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get payment history by status
     */
    @GetMapping("/history/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ManualPaymentResponseDTO>> getPaymentHistoryByStatus(
            @PathVariable String status) {
        
        PaymentManualRequest.PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentManualRequest.PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment status: {}", status);
            return ResponseEntity.badRequest().build();
        }
        
        List<ManualPaymentResponseDTO> history = paymentManualService.getRequestsByStatus(paymentStatus);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get payment history summary
     */
    @GetMapping("/history/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentHistorySummary() {
        Map<String, Object> summary = paymentManualService.getPaymentHistorySummary();
        return ResponseEntity.ok(summary);
    }
}
