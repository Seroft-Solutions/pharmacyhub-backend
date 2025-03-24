# Anti-Sharing Protection Backend Implementation Plan

This document outlines the backend implementation requirements for the Anti-Sharing Protection feature.

## Database Structure

### Create `login_sessions` Table

```sql
CREATE TABLE login_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    device_id TEXT NOT NULL,
    ip_address TEXT NOT NULL,
    country TEXT,
    user_agent TEXT,
    login_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT unique_user_device UNIQUE (user_id, device_id)
);

-- Indexes for efficient queries
CREATE INDEX ON login_sessions(user_id);
CREATE INDEX ON login_sessions(device_id);
CREATE INDEX ON login_sessions(login_time);
CREATE INDEX ON login_sessions(ip_address);
CREATE INDEX ON login_sessions(active);
```

## Entity and DTO Classes

### LoginSession Entity

```java
@Entity
@Table(name = "login_sessions")
public class LoginSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "login_time")
    private ZonedDateTime loginTime;
    
    @Column(name = "last_active")
    private ZonedDateTime lastActive;
    
    @Column(name = "active")
    private boolean active;
    
    // Getters and setters...
}
```

### LoginValidationResult DTO

```java
public enum LoginStatus {
    OK,
    NEW_DEVICE,
    SUSPICIOUS_LOCATION,
    TOO_MANY_DEVICES,
    OTP_REQUIRED
}

public class LoginValidationResult {
    private LoginStatus status;
    private String message;
    private boolean requiresOtp;
    private UUID sessionId;
    
    // Getters and setters...
}
```

## Service Implementation

### SessionValidationService

```java
@Service
public class SessionValidationService {
    private final LoginSessionRepository loginSessionRepository;
    private final GeoIpService geoIpService;
    
    // Constructor with dependencies...
    
    public LoginValidationResult validateLogin(UUID userId, String deviceId, String ipAddress, String userAgent) {
        // Check for existing sessions
        List<LoginSession> userSessions = loginSessionRepository.findActiveSessionsByUserId(userId);
        
        // Check if this device has been used before
        boolean isNewDevice = !loginSessionRepository.existsByUserIdAndDeviceId(userId, deviceId);
        
        // Check for suspicious location
        String country = geoIpService.getCountryFromIp(ipAddress);
        boolean isSuspiciousLocation = checkForSuspiciousLocation(userId, country);
        
        // Check if too many active devices
        boolean tooManyDevices = userSessions.size() >= MAX_ACTIVE_SESSIONS;
        
        // Determine validation result
        if (isNewDevice) {
            return new LoginValidationResult(LoginStatus.NEW_DEVICE, "New device detected", true, null);
        } else if (isSuspiciousLocation) {
            return new LoginValidationResult(LoginStatus.SUSPICIOUS_LOCATION, "Login from unusual location", true, null);
        } else if (tooManyDevices) {
            return new LoginValidationResult(LoginStatus.TOO_MANY_DEVICES, "Too many active devices", false, null);
        }
        
        // Create or update session
        LoginSession session = createOrUpdateSession(userId, deviceId, ipAddress, country, userAgent);
        
        return new LoginValidationResult(LoginStatus.OK, null, false, session.getId());
    }
    
    // Helper methods for validation logic...
}
```

### SessionManagementService

```java
@Service
public class SessionManagementService {
    private final LoginSessionRepository loginSessionRepository;
    
    // Constructor with dependencies...
    
    public List<LoginSession> getUserSessions(UUID userId, boolean activeOnly) {
        return activeOnly 
            ? loginSessionRepository.findActiveSessionsByUserId(userId)
            : loginSessionRepository.findAllByUserId(userId);
    }
    
    public void terminateSession(UUID sessionId) {
        loginSessionRepository.deactivateSession(sessionId);
    }
    
    public void terminateOtherSessions(UUID userId, UUID currentSessionId) {
        loginSessionRepository.deactivateOtherSessions(userId, currentSessionId);
    }
    
    public List<LoginSession> getSuspiciousSessions() {
        // Implement logic to detect suspicious sessions
        // This could include sessions from different countries, rapid switching, etc.
    }
    
    // Other session management methods...
}
```

## Controller Implementation

### SessionController

```java
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {
    private final SessionValidationService sessionValidationService;
    private final SessionManagementService sessionManagementService;
    
    // Constructor with dependencies...
    
    @PostMapping("/validate")
    public ResponseEntity<LoginValidationResult> validateLogin(@RequestBody LoginValidationRequest request) {
        LoginValidationResult result = sessionValidationService.validateLogin(
            request.getUserId(), 
            request.getDeviceId(), 
            request.getIpAddress() != null ? request.getIpAddress() : getClientIpAddress(),
            request.getUserAgent()
        );
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<LoginSessionDTO>> getUserSessions(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "false") boolean active) {
        List<LoginSession> sessions = sessionManagementService.getUserSessions(userId, active);
        List<LoginSessionDTO> sessionDTOs = convertToDTO(sessions);
        
        return ResponseEntity.ok(sessionDTOs);
    }
    
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> terminateSession(@PathVariable UUID sessionId) {
        sessionManagementService.terminateSession(sessionId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/users/{userId}/terminate-others")
    public ResponseEntity<Void> terminateOtherSessions(
            @PathVariable UUID userId,
            @RequestBody TerminateOtherSessionsRequest request) {
        sessionManagementService.terminateOtherSessions(userId, request.getCurrentSessionId());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/users/{userId}/require-otp")
    public ResponseEntity<Void> requireOtpVerification(@PathVariable UUID userId) {
        sessionManagementService.requireOtpVerification(userId);
        return ResponseEntity.ok().build();
    }
    
    // Helper methods...
}
```

### AdminSessionController

```java
@RestController
@RequestMapping("/api/v1/sessions/monitoring")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSessionController {
    private final SessionManagementService sessionManagementService;
    
    // Constructor with dependencies...
    
    @GetMapping
    public ResponseEntity<List<LoginSessionDTO>> getAllSessions(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false, defaultValue = "false") boolean active,
            @RequestParam(required = false, defaultValue = "false") boolean suspicious,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        SessionFilterCriteria criteria = new SessionFilterCriteria(
            userId, active, suspicious, fromDate, toDate
        );
        
        List<LoginSession> sessions = sessionManagementService.getSessions(criteria);
        List<LoginSessionDTO> sessionDTOs = convertToDTO(sessions);
        
        return ResponseEntity.ok(sessionDTOs);
    }
    
    // Other admin monitoring endpoints...
}
```

## Integration with Authentication Flow

1. Modify the `AuthenticationService` to include device information in the login process.
2. Update the login endpoint to include device validation.
3. Add OTP verification flow to the authentication process.

## Next Steps

1. Implement the database table and entities
2. Create repository interfaces for database access
3. Implement service layer with validation logic
4. Create REST controllers for session management
5. Integrate with existing authentication flow
6. Add GeoIP detection service
7. Create scheduled job for inactive session cleanup
