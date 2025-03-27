package com.pharmacyhub.payment.manual.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for submitting a manual payment request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualPaymentSubmitDTO {
    @NotNull(message = "Exam ID is required")
    private Long examId;
    
    @NotBlank(message = "Sender number is required")
    private String senderNumber;
    
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    private Double amount;
    
    private String notes;
    
    private String screenshotData;
}