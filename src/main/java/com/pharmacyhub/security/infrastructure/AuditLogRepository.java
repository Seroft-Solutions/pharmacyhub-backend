package com.pharmacyhub.security.infrastructure;

import com.pharmacyhub.security.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for audit logs
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs by username
     */
    List<AuditLog> findByUsername(String username);
    
    /**
     * Find audit logs by user ID
     */
    List<AuditLog> findByUserId(Long userId);
    
    /**
     * Find audit logs by action type
     */
    List<AuditLog> findByAction(String action);
    
    /**
     * Find audit logs by outcome
     */
    List<AuditLog> findByOutcome(String outcome);
    
    /**
     * Find audit logs between two timestamps
     */
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
