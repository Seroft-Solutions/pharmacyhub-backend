package com.pharmacyhub.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.easypaisa")
@Getter
@Setter
public class EasypaisaProperties {
    private String merchantId;
    private String accountNumber;
    private String secretKey;
    private String apiBaseUrl;
    private String returnUrl;
    private boolean testMode = false;
}