# RBAC Audit Logging

## Overview

PharmacyHub's RBAC system includes comprehensive audit logging to track all security-related actions. This document outlines the audit logging system's architecture, implementation, and usage.

## Audit Log Structure

### 1. Log Entry Fields
```java
public class AuditLog {
    private Long id;
    private String action;
    private String details;
    private String outcome;
    private Long userId;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
    private Map<String, Object> additionalData;
}
```

### 2. Logged Actions
- Role assignments and modifications
- Permission changes and checks
- Group modifications
- Access attempts (success/failure)
- Configuration changes
- User session activities

## API Endpoints

### 1. Log Access
```
GET     /api/audit/logs                   # Get all logs with pagination
GET     /api/audit/logs/{id}             # Get specific log entry
GET     /api/audit/logs/user/{userId}    # Get logs for specific user
GET     /api/audit/logs/action/{action}  # Get logs by action type
GET     /api/audit/logs/export           # Export logs in CSV format
```

### 2. Log Management
```
DELETE  /api/audit/logs/{id}             # Delete specific log (Admin only)
POST    /api/audit/logs/archive          # Archive old logs
GET     /api/audit/logs/statistics       # Get logging statistics
```

## Implementation Guidelines

### 1. Logging Service
```java
@Service
public class AuditService {
    // Core logging method
    public void logSecurityEvent(
        String action,
        String details,
        String outcome
    );

    // Specialized logging methods
    public void logRoleAssignment(
        Long userId, 
        Long roleId, 
        String outcome
    );

    public void logPermissionCheck(
        String resource,
        String operation,
        String outcome
    );

    public void logConfigChange(
        String component,
        String change,
        String outcome
    );
}
```

### 2. Integration Points
```java
// In RBACService
@Autowired
private AuditService auditService;

public void assignRole(Long userId, Long roleId) {
    try {
        // Role assignment logic
        auditService.logRoleAssignment(userId, roleId, "SUCCESS");
    } catch (Exception e) {
        auditService.logRoleAssignment(userId, roleId, "FAILED: " + e.getMessage());
        throw e;
    }
}
```

## Log Retention and Archival

### 1. Retention Policy
- Active logs: 90 days in main database
- Archived logs: 1 year in archive storage
- Compliance logs: 7 years in secure storage
- Regular cleanup of old logs

### 2. Archival Process
```java
@Scheduled(cron = "0 0 1 * * ?")  // Run at 1 AM daily
public void archiveOldLogs() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
    List<AuditLog> oldLogs = auditLogRepository
        .findByTimestampBefore(cutoffDate);
    
    archiveService.archiveLogs(oldLogs);
    auditLogRepository.deleteAll(oldLogs);
}
```

## Security Monitoring

### 1. Real-time Alerts
```java
@Component
public class SecurityMonitor {
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        if (isSecurityCritical(event)) {
            notificationService.sendAlert(
                "Security Alert",
                formatAlertMessage(event)
            );
        }
    }
}
```

### 2. Reporting System
- Daily security summaries
- Weekly access pattern analysis
- Monthly compliance reports
- Quarterly security reviews

## Compliance Features

### 1. Data Protection
- Encryption of sensitive log data
- Masking of personal information
- Access control for log viewers
- Tamper-evident logging

### 2. Audit Trail
- Immutable log entries
- Digital signatures
- Chain of custody tracking
- Compliance reporting

## Performance Optimization

### 1. Logging Strategy
```java
@Configuration
public class AuditConfig {
    @Bean
    public AsyncAuditLogger asyncAuditLogger() {
        return new AsyncAuditLogger(
            threadPoolExecutor(),
            logRepository
        );
    }
    
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
            2, 5, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000)
        );
    }
}
```

### 2. Caching Configuration
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager auditCacheManager() {
        return new ConcurrentMapCacheManager(
            "auditLogs",
            "userAuditLogs",
            "actionAuditLogs"
        );
    }
}
```

## Error Handling

### 1. Log Failures
```java
@ControllerAdvice
public class AuditErrorHandler {
    @ExceptionHandler(AuditLogException.class)
    public ResponseEntity<String> handleAuditError(
        AuditLogException ex
    ) {
        // Emergency logging to file system
        emergencyLogger.log(ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Audit logging failed");
    }
}
```

### 2. Recovery Procedures
- Automatic retry mechanism
- Backup logging system
- Manual recovery tools
- Data consistency checks

## Testing Guidelines

### 1. Unit Tests
```java
@Test
public void shouldLogSuccessfulRoleAssignment() {
    auditService.logRoleAssignment(userId, roleId, "SUCCESS");
    AuditLog log = auditLogRepository.findLatestByUserId(userId);
    
    assertThat(log.getAction()).isEqualTo("ROLE_ASSIGNMENT");
    assertThat(log.getOutcome()).isEqualTo("SUCCESS");
}
```

### 2. Integration Tests
```java
@SpringBootTest
public class AuditIntegrationTest {
    @Test
    public void shouldCreateAuditTrailForRoleChanges() {
        rbacService.assignRole(userId, roleId);
        
        List<AuditLog> logs = auditService
            .getUserAuditLogs(userId);
        
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getAction())
            .isEqualTo("ROLE_ASSIGNMENT");
    }
}
```

## Monitoring and Maintenance

### 1. Health Checks
```java
@Component
public class AuditHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            auditService.checkHealth();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }
}
```

### 2. Maintenance Tasks
- Regular performance monitoring
- Log analysis for patterns
- Storage optimization
- Index maintenance
