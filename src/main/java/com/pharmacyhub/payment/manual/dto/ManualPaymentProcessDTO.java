package com.pharmacyhub.payment.manual.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for processing (approving/rejecting) a manual payment request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualPaymentProcessDTO {
    private String adminNotes;
}