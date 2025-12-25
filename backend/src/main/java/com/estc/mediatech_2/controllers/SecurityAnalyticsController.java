package com.estc.mediatech_2.controllers;

import com.estc.mediatech_2.service.SecurityAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API for Security Intelligence Console.
 * Access is strictly limited to Administrators.
 */
@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SecurityAnalyticsController {

    private final SecurityAnalyticsService securityAnalyticsService;

    /**
     * Retrieves the list of high-risk users based on deterministic scoring.
     * 
     * @param limit Maximum number of users to return.
     * @return List of UserRiskProfile.
     */
    @GetMapping("/risky-users")
    public ResponseEntity<List<SecurityAnalyticsService.UserRiskProfile>> getRiskyUsers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(securityAnalyticsService.getRiskyUsers(limit));
    }

    /**
     * Retrieves aggregate statistics on high-risk financial transactions.
     * 
     * @return Map containing invoice stats.
     */
    @GetMapping("/invoice-stats")
    public ResponseEntity<Map<String, Object>> getInvoiceSecurityStats() {
        return ResponseEntity.ok(securityAnalyticsService.getInvoiceSecurityStats());
    }

    /**
     * Retrieves the simulated vulnerability report.
     * 
     * @return List of VulnerabilityReport.
     */
    @GetMapping("/vulnerabilities")
    public ResponseEntity<List<SecurityAnalyticsService.VulnerabilityReport>> getVulnerabilityReport() {
        return ResponseEntity.ok(securityAnalyticsService.getVulnerabilityReport());
    }
}
