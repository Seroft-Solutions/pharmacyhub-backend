package com.pharmacyhub.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PremiumExamInfoDTO {
    private Long examId;
    private boolean premium;
    private BigDecimal price;
    private boolean purchased;
    private boolean customPrice;
    private boolean universalAccess;
}