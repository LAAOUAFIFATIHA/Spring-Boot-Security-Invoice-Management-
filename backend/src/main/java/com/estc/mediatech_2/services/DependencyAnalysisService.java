package com.estc.mediatech_2.services;

import com.estc.mediatech_2.dto.DependencyDTO;
import com.estc.mediatech_2.dto.DependencyReportDTO;
import com.estc.mediatech_2.dto.VulnerabilityDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour analyser les dépendances et leurs vulnérabilités
 * 
 * OWASP: A06:2021 – Vulnerable and Outdated Components
 */
@Service
@Slf4j
public class DependencyAnalysisService {

    // Base de données simulée de vulnérabilités connues
    // Dans un vrai projet, utilisez l'API NVD ou OWASP Dependency-Check
    private static final Map<String, List<VulnerabilityDTO>> KNOWN_VULNERABILITIES = new HashMap<>();

    static {
        // Exemples de vulnérabilités connues (données réelles à remplacer par une vraie
        // base)
        KNOWN_VULNERABILITIES.put("org.springframework.boot:spring-boot-starter-web:3.0.0", Arrays.asList(
                VulnerabilityDTO.builder()
                        .cveId("CVE-2023-EXAMPLE")
                        .severity("MEDIUM")
                        .cvssScore(5.3)
                        .description("Example vulnerability in Spring Boot")
                        .affectedVersions("< 3.2.0")
                        .fixedVersion("3.2.0+")
                        .reference("https://nvd.nist.gov/vuln/detail/CVE-2023-EXAMPLE")
                        .build()));

        // Vulnérabilité connue dans Jackson Databind
        KNOWN_VULNERABILITIES.put("com.fasterxml.jackson.core:jackson-databind:2.15.0", Arrays.asList(
                VulnerabilityDTO.builder()
                        .cveId("CVE-2023-35116")
                        .severity("HIGH")
                        .cvssScore(7.5)
                        .description("Polymorphic Typing issue in jackson-databind")
                        .affectedVersions("< 2.15.3")
                        .fixedVersion("2.15.3+")
                        .reference("https://nvd.nist.gov/vuln/detail/CVE-2023-35116")
                        .build()));
    }

    /**
     * Génère un rapport complet des dépendances
     */
    public DependencyReportDTO generateDependencyReport() {
        log.info("🔍 Génération du rapport de dépendances...");

        List<DependencyDTO> dependencies = analyzeDependencies();

        // Calculer les statistiques
        int totalDeps = dependencies.size();
        int vulnerableDeps = (int) dependencies.stream()
                .filter(d -> d.getVulnerabilityCount() != null && d.getVulnerabilityCount() > 0)
                .count();

        Map<String, Integer> vulnerabilityCountBySeverity = calculateSeverityDistribution(dependencies);
        Map<String, Integer> dependenciesByCategory = dependencies.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getCategory() != null ? d.getCategory() : "compile",
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        int critical = vulnerabilityCountBySeverity.getOrDefault("CRITICAL", 0);
        int high = vulnerabilityCountBySeverity.getOrDefault("HIGH", 0);
        int medium = vulnerabilityCountBySeverity.getOrDefault("MEDIUM", 0);
        int low = vulnerabilityCountBySeverity.getOrDefault("LOW", 0);

        String overallRisk = calculateOverallRisk(critical, high, medium, low);

        return DependencyReportDTO.builder()
                .generatedAt(Instant.now())
                .totalDependencies(totalDeps)
                .vulnerableDependencies(vulnerableDeps)
                .vulnerabilityCountBySeverity(vulnerabilityCountBySeverity)
                .dependenciesByCategory(dependenciesByCategory)
                .dependencies(dependencies)
                .criticalCount(critical)
                .highCount(high)
                .mediumCount(medium)
                .lowCount(low)
                .overallRiskLevel(overallRisk)
                .build();
    }

    /**
     * Analyse le fichier pom.xml pour extraire les dépendances
     */
    private List<DependencyDTO> analyzeDependencies() {
        List<DependencyDTO> dependencies = new ArrayList<>();

        try {
            // Chercher le fichier pom.xml
            File pomFile = findPomFile();
            if (pomFile == null || !pomFile.exists()) {
                log.warn("❌ Fichier pom.xml non trouvé, utilisation des dépendances en dur");
                return getHardcodedDependencies();
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile);
            doc.getDocumentElement().normalize();

            NodeList dependencyNodes = doc.getElementsByTagName("dependency");

            for (int i = 0; i < dependencyNodes.getLength(); i++) {
                Element depElement = (Element) dependencyNodes.item(i);

                String groupId = getElementValue(depElement, "groupId");
                String artifactId = getElementValue(depElement, "artifactId");
                String version = getElementValue(depElement, "version");
                String scope = getElementValue(depElement, "scope");

                if (groupId != null && artifactId != null) {
                    // Vérifier les vulnérabilités
                    String depKey = groupId + ":" + artifactId + ":" + (version != null ? version : "");
                    List<VulnerabilityDTO> vulnerabilities = checkVulnerabilities(groupId, artifactId, version);

                    String riskLevel = determineRiskLevel(vulnerabilities);

                    dependencies.add(DependencyDTO.builder()
                            .groupId(groupId)
                            .artifactId(artifactId)
                            .version(version != null ? version : "N/A")
                            .scope(scope != null ? scope : "compile")
                            .category(categorizeScope(scope))
                            .vulnerabilities(vulnerabilities)
                            .vulnerabilityCount(vulnerabilities.size())
                            .riskLevel(riskLevel)
                            .build());
                }
            }

            log.info("✅ {} dépendances analysées", dependencies.size());

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'analyse du pom.xml: {}", e.getMessage());
            return getHardcodedDependencies();
        }

        return dependencies;
    }

    /**
     * Recherche le fichier pom.xml
     */
    private File findPomFile() {
        // Essayer différents chemins
        String[] possiblePaths = {
                "pom.xml",
                "../pom.xml",
                "../../pom.xml",
                System.getProperty("user.dir") + "/pom.xml",
                System.getProperty("user.dir") + "/../pom.xml"
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                log.info("✅ pom.xml trouvé: {}", file.getAbsolutePath());
                return file;
            }
        }

        return null;
    }

    /**
     * Extrait la valeur d'un élément XML
     */
    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    /**
     * Vérifie les vulnérabilités pour une dépendance
     */
    private List<VulnerabilityDTO> checkVulnerabilities(String groupId, String artifactId, String version) {
        String depKey = groupId + ":" + artifactId + ":" + version;

        // Vérifier dans notre base de vulnérabilités connues
        List<VulnerabilityDTO> vulns = KNOWN_VULNERABILITIES.getOrDefault(depKey, new ArrayList<>());

        // Vérifications supplémentaires basées sur des patterns connus
        List<VulnerabilityDTO> additionalVulns = checkCommonVulnerabilities(groupId, artifactId, version);
        vulns.addAll(additionalVulns);

        return vulns;
    }

    /**
     * Vérifie les vulnérabilités communes basées sur des patterns
     */
    private List<VulnerabilityDTO> checkCommonVulnerabilities(String groupId, String artifactId, String version) {
        List<VulnerabilityDTO> vulnerabilities = new ArrayList<>();

        // Exemple: versions anciennes de Spring
        if (groupId.contains("springframework") && version != null) {
            if (version.startsWith("5.")) {
                vulnerabilities.add(VulnerabilityDTO.builder()
                        .cveId("INFO-SPRING-OLD")
                        .severity("LOW")
                        .cvssScore(3.0)
                        .description("Version ancienne de Spring Framework détectée")
                        .affectedVersions("5.x")
                        .fixedVersion("6.x")
                        .reference("https://spring.io/security")
                        .build());
            }
        }

        // Exemple: Log4j (fameux Log4Shell)
        if (artifactId.contains("log4j") && version != null) {
            if (version.startsWith("2.1") && !version.startsWith("2.17")) {
                vulnerabilities.add(VulnerabilityDTO.builder()
                        .cveId("CVE-2021-44228")
                        .severity("CRITICAL")
                        .cvssScore(10.0)
                        .description("Log4Shell - Remote Code Execution vulnerability")
                        .affectedVersions("2.0 - 2.16.0")
                        .fixedVersion("2.17.0+")
                        .reference("https://nvd.nist.gov/vuln/detail/CVE-2021-44228")
                        .build());
            }
        }

        return vulnerabilities;
    }

    /**
     * Détermine le niveau de risque d'une dépendance
     */
    private String determineRiskLevel(List<VulnerabilityDTO> vulnerabilities) {
        if (vulnerabilities.isEmpty()) {
            return "NONE";
        }

        for (VulnerabilityDTO vuln : vulnerabilities) {
            if ("CRITICAL".equals(vuln.getSeverity())) {
                return "CRITICAL";
            }
        }

        for (VulnerabilityDTO vuln : vulnerabilities) {
            if ("HIGH".equals(vuln.getSeverity())) {
                return "HIGH";
            }
        }

        for (VulnerabilityDTO vuln : vulnerabilities) {
            if ("MEDIUM".equals(vuln.getSeverity())) {
                return "MEDIUM";
            }
        }

        return "LOW";
    }

    /**
     * Catégorise le scope d'une dépendance
     */
    private String categorizeScope(String scope) {
        if (scope == null) {
            return "compile";
        }
        return scope;
    }

    /**
     * Calcule la distribution des vulnérabilités par sévérité
     */
    private Map<String, Integer> calculateSeverityDistribution(List<DependencyDTO> dependencies) {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("CRITICAL", 0);
        distribution.put("HIGH", 0);
        distribution.put("MEDIUM", 0);
        distribution.put("LOW", 0);

        for (DependencyDTO dep : dependencies) {
            if (dep.getVulnerabilities() != null) {
                for (VulnerabilityDTO vuln : dep.getVulnerabilities()) {
                    String severity = vuln.getSeverity();
                    distribution.put(severity, distribution.getOrDefault(severity, 0) + 1);
                }
            }
        }

        return distribution;
    }

    /**
     * Calcule le risque global
     */
    private String calculateOverallRisk(int critical, int high, int medium, int low) {
        if (critical > 0) {
            return "CRITICAL";
        } else if (high > 5) {
            return "CRITICAL";
        } else if (high > 0) {
            return "HIGH";
        } else if (medium > 10) {
            return "HIGH";
        } else if (medium > 0) {
            return "MEDIUM";
        } else if (low > 0) {
            return "LOW";
        }
        return "NONE";
    }

    /**
     * Retourne les dépendances en dur si le pom.xml n'est pas accessible
     */
    private List<DependencyDTO> getHardcodedDependencies() {
        List<DependencyDTO> deps = new ArrayList<>();

        // Spring Boot
        deps.add(DependencyDTO.builder()
                .groupId("org.springframework.boot")
                .artifactId("spring-boot-starter-web")
                .version("3.2.2")
                .scope("compile")
                .category("compile")
                .vulnerabilities(new ArrayList<>())
                .vulnerabilityCount(0)
                .riskLevel("NONE")
                .build());

        deps.add(DependencyDTO.builder()
                .groupId("org.springframework.boot")
                .artifactId("spring-boot-starter-data-jpa")
                .version("3.2.2")
                .scope("compile")
                .category("compile")
                .vulnerabilities(new ArrayList<>())
                .vulnerabilityCount(0)
                .riskLevel("NONE")
                .build());

        deps.add(DependencyDTO.builder()
                .groupId("org.springframework.boot")
                .artifactId("spring-boot-starter-security")
                .version("3.2.2")
                .scope("compile")
                .category("compile")
                .vulnerabilities(new ArrayList<>())
                .vulnerabilityCount(0)
                .riskLevel("NONE")
                .build());

        // MySQL
        deps.add(DependencyDTO.builder()
                .groupId("com.mysql")
                .artifactId("mysql-connector-j")
                .version("8.2.0")
                .scope("runtime")
                .category("runtime")
                .vulnerabilities(new ArrayList<>())
                .vulnerabilityCount(0)
                .riskLevel("NONE")
                .build());

        // Jackson
        deps.add(DependencyDTO.builder()
                .groupId("com.fasterxml.jackson.core")
                .artifactId("jackson-databind")
                .version("2.15.3")
                .scope("compile")
                .category("compile")
                .vulnerabilities(new ArrayList<>())
                .vulnerabilityCount(0)
                .riskLevel("NONE")
                .build());

        // JWT
        deps.add(DependencyDTO.builder()
                .groupId("io.jsonwebtoken")
                .artifactId("jjwt-api")
                .version("0.11.5")
                .scope("compile")
                .category("compile")
                .vulnerabilities(new ArrayList<>())
                .vulnerabilityCount(0)
                .riskLevel("NONE")
                .build());

        // Lombok
        deps.add(DependencyDTO.builder()
                .groupId("org.projectlombok")
                .artifactId("lombok")
                .version("1.18.30")
                .scope("provided")
                .category("provided")
                .vulnerabilities(new ArrayList<>())
                .vulnerabilityCount(0)
                .riskLevel("NONE")
                .build());

        // iText PDF
        deps.add(DependencyDTO.builder()
                .groupId("com.itextpdf")
                .artifactId("itextpdf")
                .version("5.5.13.3")
                .scope("compile")
                .category("compile")
                .vulnerabilities(new ArrayList<>())
                .vulnerabilityCount(0)
                .riskLevel("NONE")
                .build());

        return deps;
    }
}
