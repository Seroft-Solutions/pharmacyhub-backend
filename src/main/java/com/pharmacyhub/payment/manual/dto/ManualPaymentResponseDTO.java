package com.pharmacyhub.payment.manual.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning manual payment request information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualPaymentResponseDTO {
    private Long id;
    private String userId;
    private Long examId;
    private String examTitle;
    private String senderNumber;
    private String transactionId;
    private String notes;
    private String attachmentUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String adminNotes;
}