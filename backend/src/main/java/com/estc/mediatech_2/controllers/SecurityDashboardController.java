package com.estc.mediatech_2.controllers;

import com.estc.mediatech_2.dao.SecurityEventDao;
import com.estc.mediatech_2.models.SecurityEventEntity;
import com.estc.mediatech_2.service.SecurityAnalyticsService;
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
 * Security Intelligence Dashboard Controller
 * 
 * DESIGN RATIONALE (Academic Context):
 * This controller serves as the presentation layer for the Security Operations
 * Center (SOC) module.
 * 
 * 1. SEPARATION OF CONCERNS:
 * - Analytics logic is delegated to SecurityAnalyticsService (Domain Layer).
 * - Data retrieval is delegated to SecurityEventDao (Persistence Layer).
 * - This controller handles only HTTP mapping and response formatting
 * (Interface Layer).
 * 
 * 2. DETERMINISTIC SECURITY (Why No AI?):
 * - "Explainability": Rule-based systems provide clear, audit-friendly reasons
 * for flagging (e.g. "5 failed logins").
 * AI models (especially Deep Learning) can be "black boxes" which is
 * unacceptable for forensic evidence.
 * - "Predictability": Security policies must be enforced consistently.
 * - "Efficiency": O(n) complexity vs high compute cost of ML inference.
 * - "Baseline Creation": This structured data collection is the PREREQUISITE
 * for future Anomaly Detection (Unsupervised Learning).
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
        private final SecurityAnalyticsService securityAnalyticsService;

        /**
         * Global SOC Overview
         * Aggregates key metrics for the "Single Pane of Glass" view.
         */
        @GetMapping("/dashboard")
        public ResponseEntity<Map<String, Object>> getSecurityDashboard() {
                Instant last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);

                // metrics
                long totalEvents = securityEventDao.count();
                long recentEventsCount = securityEventDao.findRecentEvents(last24Hours).size();
                long criticalEventsCount = securityEventDao.findCriticalEvents().size();

                // Analytics
                Map<String, Object> invoiceStats = securityAnalyticsService.getInvoiceSecurityStats();
                List<SecurityAnalyticsService.UserRiskProfile> topRiskyUsers = securityAnalyticsService
                                .getRiskyUsers(5);

                Map<String, Object> dashboard = new HashMap<>();

                // KPI Section
                dashboard.put("kpi", Map.of(
                                "totalLogEntries", totalEvents,
                                "events24h", recentEventsCount,
                                "criticalAlerts", criticalEventsCount,
                                "flaggedInvoices", invoiceStats.get("highValueFlagged")));

                // Charts Data
                dashboard.put("chart_severity", getSeverityDistribution(last24Hours));
                dashboard.put("chart_types", getEventTypeCountsLast24Hours());

                // Risk Tables
                dashboard.put("topRiskyUsers", topRiskyUsers);
                dashboard.put("suspiciousTransactions", invoiceStats.get("suspiciousTransactions"));

                return ResponseEntity.ok(dashboard);
        }

        /**
         * Risk Analysis API
         * Returns users classified by excessive risk behavior.
         */
        @GetMapping("/users/risk")
        public ResponseEntity<List<SecurityAnalyticsService.UserRiskProfile>> getUserRiskAnalysis(
                        @RequestParam(defaultValue = "10") int limit) {
                return ResponseEntity.ok(securityAnalyticsService.getRiskyUsers(limit));
        }

        /**
         * Invoice Pattern Analysis
         * Detects potential fraud or money laundering attempts (high value/high freq).
         */
        @GetMapping("/invoices/analysis")
        public ResponseEntity<Map<String, Object>> getInvoiceAnalysis() {
                return ResponseEntity.ok(securityAnalyticsService.getInvoiceSecurityStats());
        }

        /**
         * Timeline Data for Line Charts
         * Visualizes attack vectors over time.
         */
        @GetMapping("/events/timeline")
        public ResponseEntity<List<Map<String, Object>>> getEventTimeline(
                        @RequestParam(defaultValue = "24") int hours) {
                Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
                return ResponseEntity.ok(getAttackTimeline(since));
        }

        // ============================================================================================
        // HELPER METHODS (Data Transformation for Frontend Charts)
        // ============================================================================================

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

        private List<Map<String, Object>> getAttackTimeline(Instant since) {
                // Returns [{hour: '10:00', count: 12}, ...]
                // Optimal for Angular Charts (Chart.js / Ngx-Charts)
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
                                        map.put("timestamp", e.getKey().toString());
                                        map.put("count", e.getValue());
                                        return map;
                                })
                                .collect(Collectors.toList());
        }
}
