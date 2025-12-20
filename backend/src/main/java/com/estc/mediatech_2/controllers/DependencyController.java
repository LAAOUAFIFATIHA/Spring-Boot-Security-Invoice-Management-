package com.estc.mediatech_2.controllers;

import com.estc.mediatech_2.dto.DependencyReportDTO;
import com.estc.mediatech_2.services.DependencyAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller pour l'analyse des dépendances et vulnérabilités
 * 
 * OWASP: A06:2021 – Vulnerable and Outdated Components
 * Accès réservé aux administrateurs uniquement
 */
@RestController
@RequestMapping("/api/dependencies")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
@Slf4j
public class DependencyController {

    private final DependencyAnalysisService dependencyAnalysisService;

    /**
     * Obtenir le rapport complet des dépendances et vulnérabilités
     */
    @GetMapping("/report")
    public ResponseEntity<DependencyReportDTO> getDependencyReport() {
        log.info("📊 Admin accessing dependency report");

        DependencyReportDTO report = dependencyAnalysisService.generateDependencyReport();

        log.info("✅ Dependency report generated: {} total, {} vulnerable",
                report.getTotalDependencies(),
                report.getVulnerableDependencies());

        return ResponseEntity.ok(report);
    }
}
