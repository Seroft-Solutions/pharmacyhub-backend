package com.pharmacyhub.payment.service;

import com.google.gson.Gson;
import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.domain.repository.ExamRepository;
import com.pharmacyhub.exception.BadRequestException;
import com.pharmacyhub.exception.ResourceNotFoundException;
import com.pharmacyhub.payment.dto.PaymentDetails;
import com.pharmacyhub.payment.dto.PaymentGatewayResponse;
import com.pharmacyhub.payment.dto.PaymentInitResponse;
import com.pharmacyhub.payment.dto.PaymentResult;
import com.pharmacyhub.payment.dto.PaymentWebhookResult;
import com.pharmacyhub.payment.entity.Payment;
import com.pharmacyhub.payment.entity.Payment.PaymentMethod;
import com.pharmacyhub.payment.entity.Payment.PaymentStatus;
import com.pharmacyhub.payment.gateway.PaymentGatewayService;
import com.pharmacyhub.payment.repository.PaymentRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final ExamRepository examRepository;
    private final Map<PaymentMethod, PaymentGatewayService> paymentGateways;
    
    @Autowired
    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            ExamRepository examRepository,
            @Qualifier("jazzCashPaymentGatewayService") PaymentGatewayService jazzCashGateway,
            @Qualifier("easypaisaPaymentGatewayService") PaymentGatewayService easyPaisaGateway) {
        this.paymentRepository = paymentRepository;
        this.examRepository = examRepository;
        
        // Initialize gateway map
        this.paymentGateways = new HashMap<>();
        this.paymentGateways.put(PaymentMethod.JAZZCASH, jazzCashGateway);
        this.paymentGateways.put(PaymentMethod.EASYPAISA, easyPaisaGateway);
    }
    
    @Override
    public PaymentInitResponse initializeExamPayment(Long examId, String userId, PaymentMethod paymentMethod, String redirectUrl) {
        // Find the exam
        Exam exam = examRepository.findByIdAndDeletedFalse(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
        
        // Check if exam is premium
        if (!exam.isPremium()) {
            throw new BadRequestException("This exam is not a premium exam and does not require payment");
        }
        
        // Check if user already has purchased this exam
        if (hasUserPurchasedExam(examId, userId)) {
            throw new BadRequestException("You have already purchased this exam");
        }
        
        // Get price from exam
        BigDecimal price = exam.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid exam price");
        }
        
        // Get the appropriate payment gateway
        PaymentGatewayService gateway = paymentGateways.get(paymentMethod);
        if (gateway == null) {
            throw new BadRequestException("Unsupported payment method: " + paymentMethod);
        }
        
        // Create a description
        String description = "Payment for " + exam.getTitle();
        
        // Generate callback URL
        String callbackUrl = redirectUrl + "?examId=" + examId;
        
        // Create payment record
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAmount(price);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setMethod(paymentMethod);
        payment.setItemType("EXAM");
        payment.setItemId(examId);
        payment.setCreatedAt(LocalDateTime.now());
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Initialize payment with gateway
        PaymentGatewayResponse gatewayResponse = gateway.initializePayment(
            price, 
            description, 
            userId, 
            callbackUrl
        );
        
        // Update payment with transaction ID
        savedPayment.setTransactionId(gatewayResponse.getTransactionId());
        paymentRepository.save(savedPayment);
        
        // Build response
        PaymentInitResponse response = new PaymentInitResponse();
        response.setPaymentId(savedPayment.getId());
        response.setTransactionId(gatewayResponse.getTransactionId());
        response.setRedirectUrl(gatewayResponse.getPaymentUrl());
        response.setFormParameters(gatewayResponse.getAdditionalData());
        
        return response;
    }
    
    @Override
    public PaymentResult processPaymentCallback(String transactionId, String status, Map<String, String> additionalParams) {
        log.info("Processing payment callback for transaction: {}", transactionId);
        
        // Find the payment by transactionId
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        if (!paymentOpt.isPresent()) {
            log.error("Payment not found for transaction: {}", transactionId);
            return new PaymentResult(false, "Payment not found");
        }
        
        Payment payment = paymentOpt.get();
        PaymentGatewayService gateway = paymentGateways.get(payment.getMethod());
        
        if (gateway == null) {
            log.error("No gateway found for payment method: {}", payment.getMethod());
            return new PaymentResult(false, "Unsupported payment method");
        }
        
        // Process the webhook notification
        PaymentWebhookResult webhookResult = gateway.processWebhookNotification(additionalParams);
        
        // Update payment status
        if ("COMPLETED".equals(webhookResult.getStatus())) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());
            payment.setPaymentResponse(new Gson().toJson(additionalParams));
            paymentRepository.save(payment);
            
            return new PaymentResult(true, "Payment completed successfully");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setPaymentResponse(new Gson().toJson(additionalParams));
            paymentRepository.save(payment);
            
            return new PaymentResult(false, webhookResult.getMessage());
        }
    }
    
    @Override
    public boolean hasUserPurchasedExam(Long examId, String userId) {
        // First, check if the user has purchased any premium exam (for the "one payment unlocks all" feature)
        if (hasUserPurchasedAnyExam(userId)) {
            return true;
        }
        
        // If not, check if user has a successful payment specifically for this exam
        return paymentRepository.findByUserIdAndItemTypeAndItemId(userId, "EXAM", examId)
            .map(payment -> PaymentStatus.COMPLETED.equals(payment.getStatus()))
            .orElse(false);
    }
    
    @Override
    public boolean hasUserPurchasedAnyExam(String userId) {
        // Check if the user has any completed payment for any exam
        List<Payment> userPayments = paymentRepository.findByUserIdAndItemTypeAndStatus(
            userId, "EXAM", PaymentStatus.COMPLETED);
        
        boolean hasPurchased = !userPayments.isEmpty();
        
        // Log the result for debugging
        if (hasPurchased) {
            log.info("User {} has purchased at least one premium exam. Number of premium exams purchased: {}", 
                   userId, userPayments.size());
            
            // Log the exam IDs for reference
            String examIds = userPayments.stream()
                .map(payment -> String.valueOf(payment.getItemId()))
                .collect(Collectors.joining(", "));
            log.info("User {} has purchased these exams: {}", userId, examIds);
        } else {
            log.info("User {} has not purchased any premium exams yet", userId);
        }
        
        return hasPurchased;
    }
    
    @Override
    public List<Payment> getUserPaymentHistory(String userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public PaymentDetails getPaymentDetailsByTransaction(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + transactionId));
        
        // Get exam details
        Exam exam = examRepository.findById(payment.getItemId())
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + payment.getItemId()));
        
        PaymentDetails details = new PaymentDetails();
        details.setPaymentId(payment.getId());
        details.setAmount(payment.getAmount());
        details.setTransactionId(payment.getTransactionId());
        
        // Get gateway URL - this depends on the payment method
        PaymentGatewayService gateway = paymentGateways.get(payment.getMethod());
        if (gateway != null) {
            // In a real implementation, we'd get this from the gateway
            // For now, use a placeholder
            if (payment.getMethod() == PaymentMethod.JAZZCASH) {
                details.setGatewayUrl("https://sandbox.jazzcash.com.pk/CustomerPortal/transactionstatus.jsf");
            } else if (payment.getMethod() == PaymentMethod.EASYPAISA) {
                details.setGatewayUrl("https://easypaisa.com.pk/payment");
            }
        }
        
        // Set item name from exam
        details.setItemName(exam.getTitle());
        
        return details;
    }
}