package com.pharmacyhub.payment.repository;

import com.pharmacyhub.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(String userId);
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Payment> findByItemTypeAndItemId(String itemType, Long itemId);
    Optional<Payment> findByUserIdAndItemTypeAndItemId(String userId, String itemType, Long itemId);
    List<Payment> findByUserIdAndItemTypeAndStatus(String userId, String itemType, Payment.PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
}