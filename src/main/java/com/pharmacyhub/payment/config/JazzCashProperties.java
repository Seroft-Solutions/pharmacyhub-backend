package com.pharmacyhub.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.jazzcash")
@Getter
@Setter
public class JazzCashProperties {
    private String merchantId;
    private String password;
    private String integrityKey;
    private String apiBaseUrl;
    private String returnUrl;
    private boolean testMode = false;
}