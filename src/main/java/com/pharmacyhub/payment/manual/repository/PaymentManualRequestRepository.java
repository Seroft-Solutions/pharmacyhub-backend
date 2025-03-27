package com.pharmacyhub.payment.manual.repository;

import com.pharmacyhub.payment.manual.entity.PaymentManualRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for manual payment requests
 */
@Repository
public interface PaymentManualRequestRepository extends JpaRepository<PaymentManualRequest, Long> {
    /**
     * Find all requests for a specific user
     */
    List<PaymentManualRequest> findByUserId(String userId);
    
    /**
     * Find all requests for a specific exam
     */
    List<PaymentManualRequest> findByExamId(Long examId);
    
    /**
     * Find all requests for a specific user and exam
     */
    List<PaymentManualRequest> findByUserIdAndExamId(String userId, Long examId);
    
    /**
     * Find all requests for a specific user, exam, and status
     */
    List<PaymentManualRequest> findByUserIdAndExamIdAndStatus(
        String userId, Long examId, PaymentManualRequest.PaymentStatus status
    );
    
    /**
     * Find all requests for a specific user and status
     * Used for checking if a user has any approved payment request
     */
    List<PaymentManualRequest> findByUserIdAndStatus(
        String userId, PaymentManualRequest.PaymentStatus status
    );
    
    /**
     * Find all requests with a specific status
     */
    List<PaymentManualRequest> findByStatus(PaymentManualRequest.PaymentStatus status);
    
    /**
     * Find all requests with any of the given statuses
     */
    List<PaymentManualRequest> findByStatusIn(List<PaymentManualRequest.PaymentStatus> statuses);
}