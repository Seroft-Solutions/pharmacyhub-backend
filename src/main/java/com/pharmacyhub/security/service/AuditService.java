package com.pharmacyhub.security.service;

import com.pharmacyhub.security.domain.AuditLog;
import com.pharmacyhub.security.infrastructure.AuditLogRepository;
import com.pharmacyhub.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for auditing security events
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;

    /**
     * Log a security event
     *
     * @param action  The action performed
     * @param details Details of the action
     * @param outcome Outcome of the action (SUCCESS, FAILURE, etc.)
     */
    public void logSecurityEvent(String action, String details, String outcome) {
        String username = securityUtils.getCurrentUsername();
        Long userId = securityUtils.getCurrentUserId();
        String ipAddress = securityUtils.getCurrentClientIp();
        String userAgent = securityUtils.getCurrentUserAgent();

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .details(details)
                .outcome(outcome)
                .username(username)
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Logged security event: {}, {}, {}", action, details, outcome);
    }
}
