package com.pharmacyhub.payment.manual.service;

import com.pharmacyhub.payment.manual.dto.ManualPaymentProcessDTO;
import com.pharmacyhub.payment.manual.dto.ManualPaymentResponseDTO;
import com.pharmacyhub.payment.manual.dto.ManualPaymentSubmitDTO;
import com.pharmacyhub.payment.manual.entity.PaymentManualRequest;

import java.util.List;

/**
 * Service interface for manual payment operations
 */
public interface PaymentManualService {
    /**
     * Submit a new manual payment request
     * @param userId ID of the user making the request
     * @param request Details of the payment request
     * @return The created request
     */
    ManualPaymentResponseDTO submitRequest(String userId, ManualPaymentSubmitDTO request);
    
    /**
     * Approve a manual payment request
     * @param requestId ID of the request to approve
     * @param processDTO Processing details including admin notes
     * @return The updated request
     */
    ManualPaymentResponseDTO approveRequest(Long requestId, ManualPaymentProcessDTO processDTO);
    
    /**
     * Reject a manual payment request
     * @param requestId ID of the request to reject
     * @param processDTO Processing details including rejection reason
     * @return The updated request
     */
    ManualPaymentResponseDTO rejectRequest(Long requestId, ManualPaymentProcessDTO processDTO);
    
    /**
     * Get all payment requests for a user
     * @param userId User ID
     * @return List of payment requests
     */
    List<ManualPaymentResponseDTO> getUserRequests(String userId);
    
    /**
     * Get all payment requests (admin)
     * @return List of all payment requests
     */
    List<ManualPaymentResponseDTO> getAllRequests();
    
    /**
     * Get payment requests with a specific status (admin)
     * @param status Status to filter by
     * @return List of payment requests with the given status
     */
    List<ManualPaymentResponseDTO> getRequestsByStatus(PaymentManualRequest.PaymentStatus status);
    
    /**
     * Check if a user has a pending request for an exam
     * @param userId User ID
     * @param examId Exam ID
     * @return true if there is a pending request, false otherwise
     */
    boolean hasUserPendingRequest(String userId, Long examId);
    
    /**
     * Check if a user has an approved request for an exam
     * @param userId User ID
     * @param examId Exam ID
     * @return true if there is an approved request, false otherwise
     */
    boolean hasUserApprovedRequest(String userId, Long examId);
    
    /**
     * Checks if a user has any pending manual payment request
     * @param userId the user ID
     * @return true if user has any pending request, false otherwise
     */
    boolean hasUserAnyPendingRequest(String userId);
}