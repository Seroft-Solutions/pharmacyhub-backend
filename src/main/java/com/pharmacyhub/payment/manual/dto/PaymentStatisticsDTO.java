package com.pharmacyhub.payment.manual.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning payment statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatisticsDTO {
    private long totalUsers;
    private long paidUsers;
    private long totalAmountCollected;
    private long recentPayments;
    private double approvalRate;
    
    // Payment summary
    private long approved;
    private long rejected;
    private long pending;
}
