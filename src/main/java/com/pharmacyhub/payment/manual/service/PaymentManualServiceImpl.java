package com.pharmacyhub.payment.manual.service;

import com.pharmacyhub.domain.entity.Exam;
import com.pharmacyhub.payment.manual.dto.ManualPaymentProcessDTO;
import com.pharmacyhub.payment.manual.dto.ManualPaymentResponseDTO;
import com.pharmacyhub.payment.manual.dto.ManualPaymentSubmitDTO;
import com.pharmacyhub.payment.manual.dto.PaymentStatisticsDTO;
import com.pharmacyhub.payment.manual.entity.PaymentManualRequest;
import com.pharmacyhub.payment.manual.repository.PaymentManualRequestRepository;
import com.pharmacyhub.entity.User;
import com.pharmacyhub.service.ExamService;
import com.pharmacyhub.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final UserService userService;
    
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
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setNotes(request.getNotes());
        paymentRequest.setScreenshotData(request.getScreenshotData());
        paymentRequest.setStatus(PaymentManualRequest.PaymentStatus.PENDING);
        paymentRequest.setCreatedAt(LocalDateTime.now());
        
        PaymentManualRequest saved = repository.save(paymentRequest);
        log.info("Manual payment request submitted: {}", saved.getId());
        
        // Use a simple Map for the single exam title
        Map<Long, String> examTitles = new HashMap<>();
        try {
            examService.findById(request.getExamId())
                .ifPresent(exam -> examTitles.put(exam.getId(), exam.getTitle()));
        } catch (Exception e) {
            log.warn("Could not retrieve exam title for submission", e);
        }
        
        return mapToResponseDTO(saved, examTitles, true);
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
        
        // Use a simple Map for the single exam title
        Map<Long, String> examTitles = new HashMap<>();
        try {
            examService.findById(request.getExamId())
                .ifPresent(exam -> examTitles.put(exam.getId(), exam.getTitle()));
        } catch (Exception e) {
            log.warn("Could not retrieve exam title", e);
        }
        
        return mapToResponseDTO(saved, examTitles, true);
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
        
        // Use a simple Map for the single exam title
        Map<Long, String> examTitles = new HashMap<>();
        try {
            examService.findById(request.getExamId())
                .ifPresent(exam -> examTitles.put(exam.getId(), exam.getTitle()));
        } catch (Exception e) {
            log.warn("Could not retrieve exam title", e);
        }
        
        return mapToResponseDTO(saved, examTitles, true);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ManualPaymentResponseDTO> getUserRequests(String userId, boolean includeScreenshots) {
        List<PaymentManualRequest> userRequests = repository.findByUserId(userId);
        
        // First, collect all exam IDs we need to look up
        List<Long> examIds = userRequests.stream()
            .map(PaymentManualRequest::getExamId)
            .distinct()
            .collect(Collectors.toList());
        
        // Bulk fetch all needed exams in a single query
        Map<Long, String> examTitles = new HashMap<>();
        if (!examIds.isEmpty()) {
            try {
                // This fetches all exams in one go
                examService.findByIds(examIds).forEach(exam -> {
                    examTitles.put(exam.getId(), exam.getTitle());
                });
            } catch (Exception e) {
                log.warn("Error fetching exam titles: {}", e.getMessage());
            }
        }
        
        // Now map the requests to DTOs with the bulk-loaded exam titles
        return userRequests.stream()
            .map(request -> mapToResponseDTO(request, examTitles, includeScreenshots))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ManualPaymentResponseDTO> getAllRequests() {
        List<PaymentManualRequest> allRequests = repository.findAll();
        
        // Bulk fetch all exam titles
        List<Long> examIds = allRequests.stream()
            .map(PaymentManualRequest::getExamId)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, String> examTitles = new HashMap<>();
        if (!examIds.isEmpty()) {
            try {
                examService.findByIds(examIds).forEach(exam -> {
                    examTitles.put(exam.getId(), exam.getTitle());
                });
            } catch (Exception e) {
                log.warn("Error fetching exam titles: {}", e.getMessage());
            }
        }
        
        return allRequests.stream()
            .map(request -> mapToResponseDTO(request, examTitles, false))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ManualPaymentResponseDTO> getRequestsByStatus(PaymentManualRequest.PaymentStatus status) {
        List<PaymentManualRequest> statusRequests = repository.findByStatus(status);
        
        // Bulk fetch all exam titles
        List<Long> examIds = statusRequests.stream()
            .map(PaymentManualRequest::getExamId)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, String> examTitles = new HashMap<>();
        if (!examIds.isEmpty()) {
            try {
                examService.findByIds(examIds).forEach(exam -> {
                    examTitles.put(exam.getId(), exam.getTitle());
                });
            } catch (Exception e) {
                log.warn("Error fetching exam titles: {}", e.getMessage());
            }
        }
        
        return statusRequests.stream()
            .map(request -> mapToResponseDTO(request, examTitles, false))
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
    public boolean hasUserAnyPendingRequest(String userId) {
        return !repository.findByUserIdAndStatus(
            userId, PaymentManualRequest.PaymentStatus.PENDING
        ).isEmpty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentStatisticsDTO getPaymentStatistics() {
        // Get counts of total users, paid users, etc.
        long totalUsers = userService.findAll().size();
        
        // Count unique users with approved payments
        List<PaymentManualRequest> approvedRequests = repository.findByStatus(PaymentManualRequest.PaymentStatus.APPROVED);
        long paidUsers = approvedRequests.stream()
            .map(PaymentManualRequest::getUserId)
            .distinct()
            .count();
        
        // Calculate total amount collected (stubbed for now)
        long totalAmountCollected = approvedRequests.size() * 500; // Assuming each payment is 500 PKR
        
        // Count recent payments (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentPayments = approvedRequests.stream()
            .filter(req -> req.getProcessedAt() != null && req.getProcessedAt().isAfter(weekAgo))
            .count();
        
        // Calculate approval rate
        long totalProcessed = repository.findByStatusIn(List.of(
            PaymentManualRequest.PaymentStatus.APPROVED,
            PaymentManualRequest.PaymentStatus.REJECTED
        )).size();
        
        double approvalRate = totalProcessed > 0 ?
            (double) approvedRequests.size() / totalProcessed * 100 : 0;
        
        // Get counts for summary
        long approved = approvedRequests.size();
        long rejected = repository.findByStatus(PaymentManualRequest.PaymentStatus.REJECTED).size();
        long pending = repository.findByStatus(PaymentManualRequest.PaymentStatus.PENDING).size();
        
        PaymentStatisticsDTO stats = new PaymentStatisticsDTO();
        stats.setTotalUsers(totalUsers);
        stats.setPaidUsers(paidUsers);
        stats.setTotalAmountCollected(totalAmountCollected);
        stats.setRecentPayments(recentPayments);
        stats.setApprovalRate(approvalRate);
        stats.setApproved(approved);
        stats.setRejected(rejected);
        stats.setPending(pending);
        
        return stats;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentHistorySummary() {
        // Get counts for different statuses
        long approved = repository.findByStatus(PaymentManualRequest.PaymentStatus.APPROVED).size();
        long rejected = repository.findByStatus(PaymentManualRequest.PaymentStatus.REJECTED).size();
        long pending = repository.findByStatus(PaymentManualRequest.PaymentStatus.PENDING).size();
        
        // Calculate total amount (stubbed for now)
        long totalAmount = approved * 500; // Assuming each payment is 500 PKR
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("approved", approved);
        summary.put("rejected", rejected);
        summary.put("pending", pending);
        summary.put("totalAmount", totalAmount);
        
        return summary;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserApprovedRequest(String userId, Long examId) {
        // If examId is null, check if the user has any approved manual payment request
        if (examId == null) {
            List<PaymentManualRequest> approvedRequests = repository.findByUserIdAndStatus(
                userId, PaymentManualRequest.PaymentStatus.APPROVED
            );
            boolean hasAnyApproved = !approvedRequests.isEmpty();
            
            if (hasAnyApproved) {
                log.info("User {} has at least one approved manual payment request", userId);
            }
            
            return hasAnyApproved;
        }
        
        // Check for a specific exam
        boolean hasSpecificApproved = !repository.findByUserIdAndExamIdAndStatus(
            userId, examId, PaymentManualRequest.PaymentStatus.APPROVED
        ).isEmpty();
        
        if (hasSpecificApproved) {
            log.info("User {} has an approved manual payment request for exam {}", userId, examId);
        }
        
        return hasSpecificApproved;
    }
    
    /**
     * Maps a PaymentManualRequest entity to a ManualPaymentResponseDTO
     * with optional screenshot data and pre-loaded exam titles
     */
    private ManualPaymentResponseDTO mapToResponseDTO(PaymentManualRequest request, Map<Long, String> examTitles, boolean includeScreenshots) {
        ManualPaymentResponseDTO dto = new ManualPaymentResponseDTO();
        dto.setId(request.getId());
        dto.setUserId(request.getUserId());
        dto.setExamId(request.getExamId());
        dto.setSenderNumber(request.getSenderNumber());
        dto.setTransactionId(request.getTransactionId());
        dto.setAmount(request.getAmount());
        
        // Add user information
        try {
            User user = userService.findByEmail(request.getUserId());
            if (user != null) {
                dto.setUserEmail(user.getEmailAddress());
                dto.setUserFirstName(user.getFirstName());
                dto.setUserLastName(user.getLastName());
                dto.setUserPhoneNumber(user.getContactNumber());
            }
        } catch (Exception e) {
            log.warn("Could not retrieve user details for ID: {}", request.getUserId(), e);
        }
        dto.setNotes(request.getNotes());
        dto.setAttachmentUrl(request.getAttachmentUrl());
        
        // Only include screenshot data if specifically requested
        if (includeScreenshots) {
            dto.setScreenshotData(request.getScreenshotData());
        }
        
        dto.setStatus(request.getStatus().toString());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setProcessedAt(request.getProcessedAt());
        dto.setAdminNotes(request.getAdminNotes());
        
        // Use pre-loaded exam title if available
        if (examTitles != null && examTitles.containsKey(request.getExamId())) {
            dto.setExamTitle(examTitles.get(request.getExamId()));
        } else {
            // Fall back to individual lookup if not in pre-loaded map
            try {
                String examTitle = examService.findById(request.getExamId())
                    .map(Exam::getTitle)
                    .orElse("Unknown Exam");
                dto.setExamTitle(examTitle);
            } catch (Exception e) {
                log.warn("Could not retrieve exam title for ID: {}", request.getExamId(), e);
                dto.setExamTitle("Unknown Exam");
            }
        }
        
        return dto;
    }
}