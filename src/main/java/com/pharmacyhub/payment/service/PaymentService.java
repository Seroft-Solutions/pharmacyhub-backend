package com.pharmacyhub.payment.service;

import com.pharmacyhub.payment.dto.PaymentDetails;
import com.pharmacyhub.payment.dto.PaymentInitResponse;
import com.pharmacyhub.payment.dto.PaymentResult;
import com.pharmacyhub.payment.entity.Payment;
import com.pharmacyhub.payment.entity.Payment.PaymentMethod;

import java.util.List;
import java.util.Map;

/**
 * Service for payment operations
 */
public interface PaymentService {
    /**
     * Initialize a payment for an exam
     * @param examId ID of the exam to purchase
     * @param userId ID of the user making the purchase
     * @param paymentMethod Payment method to use
     * @param redirectUrl URL to redirect after payment 
     * @return Payment response with redirect URL
     */
    PaymentInitResponse initializeExamPayment(Long examId, String userId, PaymentMethod paymentMethod, String redirectUrl);
    
    /**
     * Process payment callback from gateway
     * @param transactionId Transaction ID from the gateway
     * @param status Status from the gateway
     * @param additionalParams Additional parameters from the gateway
     * @return Processed payment result
     */
    PaymentResult processPaymentCallback(String transactionId, String status, Map<String, String> additionalParams);
    
    /**
     * Check if a user has purchased access to an exam
     * @param examId Exam ID to check
     * @param userId User ID to check
     * @return true if the user has valid access, false otherwise
     */
    boolean hasUserPurchasedExam(Long examId, String userId);
    
    /**
     * Check if a user has purchased any premium exam, granting access to all premium exams
     * @param userId User ID to check
     * @return true if the user has purchased any premium exam, false otherwise
     */
    boolean hasUserPurchasedAnyExam(String userId);
    
    /**
     * Get payment history for a user
     * @param userId User ID
     * @return List of payment records
     */
    List<Payment> getUserPaymentHistory(String userId);
    
    /**
     * Get payment details by transaction ID
     * @param transactionId Transaction ID
     * @return Payment details
     */
    PaymentDetails getPaymentDetailsByTransaction(String transactionId);
    
    /**
     * Get payment status for a specific exam and user
     * @param examId Exam ID to check
     * @param userId User ID to check
     * @return Payment status as a string: PAID, PENDING, FAILED, NOT_PAID, or UNKNOWN
     */
    String getUserPaymentStatusForExam(Long examId, String userId);
}