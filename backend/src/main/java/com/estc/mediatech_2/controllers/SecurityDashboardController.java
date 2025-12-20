package com.estc.mediatech_2.controllers;

import com.estc.mediatech_2.dao.SecurityEventDao;
import com.estc.mediatech_2.models.SecurityEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Security Dashboard Controller for Admin
 * 
 * Provides security monitoring and analytics endpoints:
 * - Recent security events
 * - Critical incidents
 * - Attack statistics
 * - User activity monitoring
 * - Threat intelligence
 * 
 * OWASP: A09:2021 – Security Logging and Monitoring Failures
 */
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
@Slf4j
public class SecurityDashboardController {

        private final SecurityEventDao securityEventDao;

        /**
         * Get security dashboard overview
         */
        @GetMapping("/dashboard")
        public ResponseEntity<Map<String, Object>> getSecurityDashboard() {
                log.info("Security dashboard accessed");

                Instant last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);
                Instant last7Days = Instant.now().minus(7, ChronoUnit.DAYS);

                // Get statistics
                long totalEvents = securityEventDao.count();
                long recentEvents = securityEventDao.findRecentEvents(last24Hours).size();
                long criticalEvents = securityEventDao.findCriticalEvents().size();

                // Event type counts
                Map<String, Long> eventTypeCounts = getEventTypeCountsLast24Hours();

                // Severity distribution
                Map<String, Long> severityDistribution = getSeverityDistribution(last24Hours);

                // Top attacked resources
                List<Map<String, Object>> topAttackedResources = getTopAttackedResources(last7Days);

                // Recent critical incidents
                List<SecurityEventEntity> recentCritical = securityEventDao.findCriticalEvents()
                                .stream()
                                .limit(10)
                                .collect(Collectors.toList());

                Map<String, Object> dashboard = new HashMap<>();
                dashboard.put("summary", Map.of(
                                "totalEvents", totalEvents,
                                "last24Hours", recentEvents,
                                "criticalIncidents", criticalEvents));
                dashboard.put("eventTypes", eventTypeCounts);
                dashboard.put("severityDistribution", severityDistribution);
                dashboard.put("topAttackedResources", topAttackedResources);
                dashboard.put("recentCriticalIncidents", recentCritical);
                dashboard.put("timestamp", Instant.now());

                return ResponseEntity.ok(dashboard);
        }

        /**
         * Get recent security events (last 24 hours)
         */
        @GetMapping("/events/recent")
        public ResponseEntity<List<SecurityEventEntity>> getRecentEvents(
                        @RequestParam(defaultValue = "24") int hours) {

                Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
                List<SecurityEventEntity> events = securityEventDao.findRecentEvents(since);

                log.info("Retrieved {} security events from last {} hours", events.size(), hours);
                return ResponseEntity.ok(events);
        }

        /**
         * Get critical security incidents
         */
        @GetMapping("/events/critical")
        public ResponseEntity<List<SecurityEventEntity>> getCriticalEvents() {
                List<SecurityEventEntity> events = securityEventDao.findCriticalEvents();
                log.info("Retrieved {} critical security events", events.size());
                return ResponseEntity.ok(events);
        }

        /**
         * Get events by type
         */
        @GetMapping("/events/type/{eventType}")
        public ResponseEntity<List<SecurityEventEntity>> getEventsByType(
                        @PathVariable String eventType) {

                List<SecurityEventEntity> events = securityEventDao.findByEventType(eventType);
                log.info("Retrieved {} events of type {}", events.size(), eventType);
                return ResponseEntity.ok(events);
        }

        /**
         * Get events by username
         */
        @GetMapping("/events/user/{username}")
        public ResponseEntity<List<SecurityEventEntity>> getEventsByUsername(
                        @PathVariable String username) {

                List<SecurityEventEntity> events = securityEventDao.findByUsername(username);
                log.info("Retrieved {} events for user {}", events.size(), username);
                return ResponseEntity.ok(events);
        }

        /**
         * Get events by IP address
         */
        @GetMapping("/events/ip/{ipAddress}")
        public ResponseEntity<List<SecurityEventEntity>> getEventsByIP(
                        @PathVariable String ipAddress) {

                List<SecurityEventEntity> events = securityEventDao.findByIpAddress(ipAddress);
                log.info("Retrieved {} events from IP {}", events.size(), ipAddress);
                return ResponseEntity.ok(events);
        }

        /**
         * Get attack statistics
         */
        @GetMapping("/statistics")
        public ResponseEntity<Map<String, Object>> getSecurityStatistics(
                        @RequestParam(defaultValue = "7") int days) {

                Instant since = Instant.now().minus(days, ChronoUnit.DAYS);

                Map<String, Object> stats = new HashMap<>();
                stats.put("period", days + " days");
                stats.put("eventTypes", getEventTypeStats(since));
                stats.put("topAttackers", getTopAttackers(since));
                stats.put("attackTimeline", getAttackTimeline(since));
                stats.put("riskScore", calculateRiskScore(since));

                return ResponseEntity.ok(stats);
        }

        /**
         * Get threat intelligence summary
         */
        @GetMapping("/threats")
        public ResponseEntity<Map<String, Object>> getThreatIntelligence() {
                Map<String, Object> threats = new HashMap<>();

                Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);

                // Count different attack types
                long sqlInjectionAttempts = securityEventDao.countByEventTypeAndTimestampBetween(
                                "SQL_INJECTION_ATTEMPT", last24h, Instant.now());

                long idorAttempts = securityEventDao.countByEventTypeAndTimestampBetween(
                                "IDOR_ATTACK", last24h, Instant.now());

                long authFailures = securityEventDao.countByEventTypeAndTimestampBetween(
                                "AUTHENTICATION_FAILURE", last24h, Instant.now());

                long massAssignmentAttempts = securityEventDao.countByEventTypeAndTimestampBetween(
                                "MASS_ASSIGNMENT_ATTEMPT", last24h, Instant.now());

                threats.put("sqlInjectionAttempts", sqlInjectionAttempts);
                threats.put("idorAttempts", idorAttempts);
                threats.put("authenticationFailures", authFailures);
                threats.put("massAssignmentAttempts", massAssignmentAttempts);
                threats.put("totalThreats",
                                sqlInjectionAttempts + idorAttempts + authFailures + massAssignmentAttempts);
                threats.put("timestamp", Instant.now());

                return ResponseEntity.ok(threats);
        }

        // Helper methods

        private Map<String, Long> getEventTypeCountsLast24Hours() {
                Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
                return securityEventDao.findRecentEvents(since)
                                .stream()
                                .collect(Collectors.groupingBy(
                                                SecurityEventEntity::getEventType,
                                                Collectors.counting()));
        }

        private Map<String, Long> getSeverityDistribution(Instant since) {
                return securityEventDao.findRecentEvents(since)
                                .stream()
                                .collect(Collectors.groupingBy(
                                                SecurityEventEntity::getSeverity,
                                                Collectors.counting()));
        }

        private List<Map<String, Object>> getTopAttackedResources(Instant since) {
                return securityEventDao.findRecentEvents(since)
                                .stream()
                                .filter(e -> e.getResource() != null)
                                .collect(Collectors.groupingBy(
                                                SecurityEventEntity::getResource,
                                                Collectors.counting()))
                                .entrySet()
                                .stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .limit(10)
                                .map(e -> {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("resource", e.getKey());
                                        map.put("count", e.getValue());
                                        return map;
                                })
                                .collect(Collectors.toList());
        }

        private Map<String, Long> getEventTypeStats(Instant since) {
                return securityEventDao.findRecentEvents(since)
                                .stream()
                                .collect(Collectors.groupingBy(
                                                SecurityEventEntity::getEventType,
                                                Collectors.counting()));
        }

        private List<Map<String, Object>> getTopAttackers(Instant since) {
                return securityEventDao.findRecentEvents(since)
                                .stream()
                                .filter(e -> e.getIpAddress() != null)
                                .collect(Collectors.groupingBy(
                                                SecurityEventEntity::getIpAddress,
                                                Collectors.counting()))
                                .entrySet()
                                .stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .limit(10)
                                .map(e -> {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("ipAddress", e.getKey());
                                        map.put("attacks", e.getValue());
                                        return map;
                                })
                                .collect(Collectors.toList());
        }

        private List<Map<String, Object>> getAttackTimeline(Instant since) {
                // Group by hour
                return securityEventDao.findRecentEvents(since)
                                .stream()
                                .collect(Collectors.groupingBy(
                                                e -> e.getTimestamp().truncatedTo(ChronoUnit.HOURS),
                                                Collectors.counting()))
                                .entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(e -> {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("hour", e.getKey());
                                        map.put("count", e.getValue());
                                        return map;
                                })
                                .collect(Collectors.toList());
        }

        private int calculateRiskScore(Instant since) {
                List<SecurityEventEntity> events = securityEventDao.findRecentEvents(since);

                int score = 0;
                for (SecurityEventEntity event : events) {
                        switch (event.getSeverity()) {
                                case "CRITICAL" -> score += 10;
                                case "WARN" -> score += 3;
                                case "INFO" -> score += 1;
                        }
                }

                // Normalize to 0-100
                return Math.min(100, score);
        }
}
