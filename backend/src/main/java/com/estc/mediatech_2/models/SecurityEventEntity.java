package com.estc.mediatech_2.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Security Event Entity for Audit Trail
 * 
 * Stores all security-relevant events for forensic analysis:
 * - Authentication failures
 * - Unauthorized access attempts
 * - Security incidents (IDOR, SQL Injection, etc.)
 * - Suspicious activities
 * 
 * OWASP: A09:2021 – Security Logging and Monitoring Failures
 */
@Entity
@Table(name = "security_events", indexes = {
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_severity", columnList = "severity"),
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_ip_address", columnList = "ip_address")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of security event
     * Examples: UNAUTHORIZED_ACCESS, SQL_INJECTION_ATTEMPT, IDOR_ATTACK
     */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /**
     * Severity level: INFO, WARN, CRITICAL
     */
    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    /**
     * Username involved (if authenticated)
     */
    @Column(name = "username", length = 100)
    private String username;

    /**
     * Resource accessed or targeted
     */
    @Column(name = "resource", length = 500)
    private String resource;

    /**
     * HTTP method used
     */
    @Column(name = "method", length = 10)
    private String method;

    /**
     * IP address of the client
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Additional details about the event
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * Timestamp of the event
     */
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}
