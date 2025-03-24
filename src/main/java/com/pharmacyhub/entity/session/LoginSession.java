package com.pharmacyhub.entity.session;

import com.pharmacyhub.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity for tracking user login sessions for anti-sharing protection
 */
@Entity
@Table(name = "login_sessions", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "device_id"})
       },
       indexes = {
           @Index(name = "idx_login_sessions_user_id", columnList = "user_id"),
           @Index(name = "idx_login_sessions_device_id", columnList = "device_id"),
           @Index(name = "idx_login_sessions_login_time", columnList = "login_time"),
           @Index(name = "idx_login_sessions_ip_address", columnList = "ip_address"),
           @Index(name = "idx_login_sessions_active", columnList = "active")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "login_time")
    @Builder.Default
    private ZonedDateTime loginTime = ZonedDateTime.now();
    
    @Column(name = "last_active")
    @Builder.Default
    private ZonedDateTime lastActive = ZonedDateTime.now();
    
    @Column(name = "active")
    @Builder.Default
    private boolean active = true;
    
    /**
     * Additional metadata about the session, stored as JSON
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;
    
    /**
     * Flag to indicate if OTP verification is required for this session
     */
    @Column(name = "requires_otp")
    @Builder.Default
    private boolean requiresOtp = false;
    
    /**
     * Flag to indicate if this session has been verified with OTP
     */
    @Column(name = "otp_verified")
    @Builder.Default
    private boolean otpVerified = false;
}
