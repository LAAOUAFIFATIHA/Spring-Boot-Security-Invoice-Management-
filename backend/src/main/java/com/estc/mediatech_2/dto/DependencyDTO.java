package com.estc.mediatech_2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour les dépendances de l'application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependencyDTO {
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private String category; // compile, runtime, test, provided
    private List<VulnerabilityDTO> vulnerabilities;
    private Integer vulnerabilityCount;
    private String riskLevel; // NONE, LOW, MEDIUM, HIGH, CRITICAL
}
