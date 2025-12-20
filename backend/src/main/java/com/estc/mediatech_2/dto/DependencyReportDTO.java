package com.estc.mediatech_2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Rapport complet des dépendances et vulnérabilités
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependencyReportDTO {
    private Instant generatedAt;
    private Integer totalDependencies;
    private Integer vulnerableDependencies;
    private Map<String, Integer> vulnerabilityCountBySeverity;
    private Map<String, Integer> dependenciesByCategory;
    private List<DependencyDTO> dependencies;
    private Integer criticalCount;
    private Integer highCount;
    private Integer mediumCount;
    private Integer lowCount;
    private String overallRiskLevel;
}
