export interface Vulnerability {
    cveId: string;
    severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
    cvssScore: number;
    description: string;
    affectedVersions: string;
    fixedVersion: string;
    reference: string;
}

export interface Dependency {
    groupId: string;
    artifactId: string;
    version: string;
    scope: string;
    category: string;
    vulnerabilities: Vulnerability[];
    vulnerabilityCount: number;
    riskLevel: 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

export interface DependencyReport {
    generatedAt: string;
    totalDependencies: number;
    vulnerableDependencies: number;
    vulnerabilityCountBySeverity: { [key: string]: number };
    dependenciesByCategory: { [key: string]: number };
    dependencies: Dependency[];
    criticalCount: number;
    highCount: number;
    mediumCount: number;
    lowCount: number;
    overallRiskLevel: 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}
