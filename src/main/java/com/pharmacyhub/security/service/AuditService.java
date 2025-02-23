package com.pharmacyhub.security.service;

import com.pharmacyhub.engine.PHEngine;
import com.pharmacyhub.security.domain.AuditLog;
import com.pharmacyhub.security.infrastructure.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService extends PHEngine {
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logSecurityEvent(String action, String details, String outcome) {
        AuditLog log = AuditLog.builder()
            .action(action)
            .details(details)
            .outcome(outcome)
            .userId(getLoggedInUser().getId())
            .timestamp(LocalDateTime.now())
            .build();
        
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getUserAuditLogs(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }
}