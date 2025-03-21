package com.pharmacyhub.payment.manual.service;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.payment.manual.dto.ManualPaymentProcessDTO;
import com.pharmacyhub.payment.manual.dto.ManualPaymentResponseDTO;
import com.pharmacyhub.payment.manual.dto.ManualPaymentSubmitDTO;
import com.pharmacyhub.payment.manual.entity.PaymentManualRequest;
import com.pharmacyhub.payment.manual.repository.PaymentManualRequestRepository;
import com.pharmacyhub.service.ExamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the PaymentManualService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentManualServiceImpl implements PaymentManualService {
    
    private final PaymentManualRequestRepository repository;
    private final ExamService examService;
    
    @Override
    @Transactional
    public ManualPaymentResponseDTO submitRequest(String userId, ManualPaymentSubmitDTO request) {
        // Check if user already has a pending request for this exam
        if (hasUserPendingRequest(userId, request.getExamId())) {
            throw new IllegalStateException("You already have a pending payment request for this exam");
        }
        
        PaymentManualRequest paymentRequest = new PaymentManualRequest();
        paymentRequest.setUserId(userId);
        paymentRequest.setExamId(request.getExamId());
        paymentRequest.setSenderNumber(request.getSenderNumber());
        paymentRequest.setTransactionId(request.getTransactionId());
        paymentRequest.setNotes(request.getNotes());
        paymentRequest.setStatus(PaymentManualRequest.PaymentStatus.PENDING);
        paymentRequest.setCreatedAt(LocalDateTime.now());
        
        PaymentManualRequest saved = repository.save(paymentRequest);
        log.info("Manual payment request submitted: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }
    
    @Override
    @Transactional
    public ManualPaymentResponseDTO approveRequest(Long requestId, ManualPaymentProcessDTO processDTO) {
        PaymentManualRequest request = repository.findById(requestId)
            .orElseThrow(() -> new EntityNotFoundException("Payment request not found with ID: " + requestId));
        
        request.setStatus(PaymentManualRequest.PaymentStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        request.setAdminNotes(processDTO.getAdminNotes());
        
        PaymentManualRequest saved = repository.save(request);
        log.info("Manual payment request approved: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }
    
    @Override
    @Transactional
    public ManualPaymentResponseDTO rejectRequest(Long requestId, ManualPaymentProcessDTO processDTO) {
        PaymentManualRequest request = repository.findById(requestId)
            .orElseThrow(() -> new EntityNotFoundException("Payment request not found with ID: " + requestId));
        
        request.setStatus(PaymentManualRequest.PaymentStatus.REJECTED);
        request.setProcessedAt(LocalDateTime.now());
        request.setAdminNotes(processDTO.getAdminNotes());
        
        PaymentManualRequest saved = repository.save(request);
        log.info("Manual payment request rejected: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ManualPaymentResponseDTO> getUserRequests(String userId) {
        return repository.findByUserId(userId).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ManualPaymentResponseDTO> getAllRequests() {
        return repository.findAll().stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ManualPaymentResponseDTO> getRequestsByStatus(PaymentManualRequest.PaymentStatus status) {
        return repository.findByStatus(status).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPendingRequest(String userId, Long examId) {
        return !repository.findByUserIdAndExamIdAndStatus(
            userId, examId, PaymentManualRequest.PaymentStatus.PENDING
        ).isEmpty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserApprovedRequest(String userId, Long examId) {
        return !repository.findByUserIdAndExamIdAndStatus(
            userId, examId, PaymentManualRequest.PaymentStatus.APPROVED
        ).isEmpty();
    }
    
    /**
     * Maps a PaymentManualRequest entity to a ManualPaymentResponseDTO
     */
    private ManualPaymentResponseDTO mapToResponseDTO(PaymentManualRequest request) {
        ManualPaymentResponseDTO dto = new ManualPaymentResponseDTO();
        dto.setId(request.getId());
        dto.setUserId(request.getUserId());
        dto.setExamId(request.getExamId());
        dto.setSenderNumber(request.getSenderNumber());
        dto.setTransactionId(request.getTransactionId());
        dto.setNotes(request.getNotes());
        dto.setAttachmentUrl(request.getAttachmentUrl());
        dto.setStatus(request.getStatus().toString());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setProcessedAt(request.getProcessedAt());
        dto.setAdminNotes(request.getAdminNotes());
        
        // Try to get exam title
        try {
            String examTitle = examService.findById(request.getExamId())
                .map(exam -> exam.getTitle())
                .orElse("Unknown Exam");
            dto.setExamTitle(examTitle);
        } catch (Exception e) {
            log.warn("Could not retrieve exam title for ID: {}", request.getExamId(), e);
            dto.setExamTitle("Unknown Exam");
        }
        
        return dto;
    }
}