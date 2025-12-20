package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dao.SecurityEventDao;
import com.estc.mediatech_2.models.SecurityEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Security Audit Service for Forensic Analysis
 * 
 * Features:
 * - Structured security event logging
 * - Severity classification (INFO, WARN, CRITICAL)
 * - Asynchronous logging for performance
 * - Forensic analysis support
 * - Automatic cleanup of old events
 * 
 * OWASP: A09:2021 – Security Logging and Monitoring Failures
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {

    private final SecurityEventDao securityEventDao;

    /**
     * Log unauthorized access attempts
     * Severity: WARN
     */
    @Async
    public void logUnauthorizedAccess(String username, String resource, String method,
            String ipAddress, String details) {
        SecurityEventEntity event = SecurityEventEntity.builder()
                .eventType("UNAUTHORIZED_ACCESS")
                .severity("WARN")
                .username(username != null ? username : "anonymous")
                .resource(resource)
                .ipAddress(ipAddress)
                .method(method)
                .details(details)
                .timestamp(Instant.now())
                .build();

        securityEventDao.save(event);

        log.warn("⚠️  SECURITY EVENT - Type: UNAUTHORIZED_ACCESS, User: {}, Resource: {}, IP: {}",
                username, resource, ipAddress);
    }

    /**
     * Log authentication failures
     * Severity: WARN
     */
    @Async
    public void logAuthenticationFailure(String username, String ipAddress,
            String reason, String userAgent) {
        SecurityEventEntity event = SecurityEventEntity.builder()
                .eventType("AUTHENTICATION_FAILURE")
                .severity("WARN")
                .username(username)
                .ipAddress(ipAddress)
                .details("Reason: " + reason + ", UserAgent: " + userAgent)
                .timestamp(Instant.now())
                .build();

        securityEventDao.save(event);

        log.warn("🔒 SECURITY EVENT - Type: AUTHENTICATION_FAILURE, User: {}, IP: {}, Reason: {}",
                username, ipAddress, reason);
    }

    /**
     * Log suspicious activities
     * Severity: WARN
     */
    @Async
    public void logSuspiciousActivity(String username, String activityType,
            String ipAddress, String details) {
        SecurityEventEntity event = SecurityEventEntity.builder()
                .eventType("SUSPICIOUS_ACTIVITY")
                .severity("WARN")
                .username(username)
                .ipAddress(ipAddress)
                .details(activityType + ": " + details)
                .timestamp(Instant.now())
                .build();

        securityEventDao.save(event);

        log.warn("⚡ SECURITY EVENT - Type: SUSPICIOUS_ACTIVITY, User: {}, Activity: {}, IP: {}",
                username, activityType, ipAddress);
    }

    /**
     * Log security incidents
     * Severity: CRITICAL
     */
    @Async
    public void logSecurityIncident(String username, String incidentType,
            String resource, String ipAddress, String details) {
        SecurityEventEntity event = SecurityEventEntity.builder()
                .eventType(incidentType)
                .severity("CRITICAL")
                .username(username)
                .resource(resource)
                .ipAddress(ipAddress)
                .details(details)
                .timestamp(Instant.now())
                .build();

        securityEventDao.save(event);

        log.error("🚨 CRITICAL SECURITY INCIDENT - Type: {}, User: {}, Resource: {}, IP: {}, Details: {}",
                incidentType, username, resource, ipAddress, details);
    }

    /**
     * Log critical security incidents (highest severity)
     * Severity: CRITICAL
     */
    @Async
    public void logCriticalSecurityIncident(String username, String incidentType,
            String resource, String ipAddress, String details) {
        SecurityEventEntity event = SecurityEventEntity.builder()
                .eventType(incidentType)
                .severity("CRITICAL")
                .username(username)
                .resource(resource)
                .ipAddress(ipAddress)
                .details(details)
                .timestamp(Instant.now())
                .build();

        securityEventDao.save(event);

        log.error("🚨🚨🚨 CRITICAL SECURITY INCIDENT - Type: {}, User: {}, Resource: {}, IP: {}",
                incidentType, username, resource, ipAddress);

        // In production, trigger alerts (email, Slack, PagerDuty, etc.)
        triggerSecurityAlert(event);
    }

    /**
     * Log successful security-relevant actions
     * Severity: INFO
     */
    @Async
    public void logSecurityAction(String username, String action, String details) {
        SecurityEventEntity event = SecurityEventEntity.builder()
                .eventType(action)
                .severity("INFO")
                .username(username)
                .details(details)
                .timestamp(Instant.now())
                .build();

        securityEventDao.save(event);

        log.info("ℹ️  SECURITY ACTION - Type: {}, User: {}, Details: {}",
                action, username, details);
    }

    /**
     * Cleanup old security events (retention: 90 days)
     * Runs daily at 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void cleanupOldSecurityEvents() {
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        long deletedCount = securityEventDao.deleteByTimestampBefore(cutoff);

        log.info("🧹 Security Event Cleanup - Deleted {} events older than 90 days", deletedCount);
    }

    /**
     * Trigger security alerts for critical incidents
     * In production: integrate with monitoring systems
     */
    private void triggerSecurityAlert(SecurityEventEntity event) {
        // TODO: Implement in production
        // - Send email to security team
        // - Post to Slack/Discord
        // - Trigger PagerDuty
        // - Send SMS for critical incidents

        log.error("🔔 ALERT TRIGGERED - Incident: {}, User: {}, IP: {}",
                event.getEventType(), event.getUsername(), event.getIpAddress());
    }
}
