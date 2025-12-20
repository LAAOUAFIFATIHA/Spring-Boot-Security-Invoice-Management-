import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DependencyReport } from '../models/dependency';

@Injectable({
    providedIn: 'root'
})
export class DependencyService {
    private apiUrl = 'http://localhost:8090/api/dependencies';

    constructor(private http: HttpClient) { }

    /**
     * Récupère le rapport complet des dépendances et vulnérabilités
     */
    getDependencyReport(): Observable<DependencyReport> {
        return this.http.get<DependencyReport>(`${this.apiUrl}/report`).pipe(
            catchError(error => {
                console.error('API Error, returning mock data:', error);
                return of(this.getMockReport());
            })
        );
    }

    private getMockReport(): DependencyReport {
        return {
            generatedAt: new Date().toISOString(),
            totalDependencies: 12,
            vulnerableDependencies: 3,
            vulnerabilityCountBySeverity: {
                'CRITICAL': 1,
                'HIGH': 1,
                'MEDIUM': 1,
                'LOW': 0
            },
            dependenciesByCategory: {
                'compile': 10,
                'runtime': 2
            },
            criticalCount: 1,
            highCount: 1,
            mediumCount: 1,
            lowCount: 0,
            overallRiskLevel: 'CRITICAL',
            dependencies: [
                {
                    groupId: 'org.springframework.boot',
                    artifactId: 'spring-boot-starter-web',
                    version: '3.0.0',
                    scope: 'compile',
                    category: 'compile',
                    vulnerabilityCount: 1,
                    riskLevel: 'MEDIUM',
                    vulnerabilities: [
                        {
                            cveId: 'CVE-2023-EXAMPLE',
                            severity: 'MEDIUM',
                            cvssScore: 5.3,
                            description: 'Example vulnerability description for Spring Boot Web.',
                            affectedVersions: '< 3.2.0',
                            fixedVersion: '3.2.0+',
                            reference: 'https://nvd.nist.gov/'
                        }
                    ]
                },
                {
                    groupId: 'com.fasterxml.jackson.core',
                    artifactId: 'jackson-databind',
                    version: '2.14.0',
                    scope: 'compile',
                    category: 'compile',
                    vulnerabilityCount: 1,
                    riskLevel: 'HIGH',
                    vulnerabilities: [
                        {
                            cveId: 'CVE-2023-35116',
                            severity: 'HIGH',
                            cvssScore: 7.5,
                            description: 'Polymorphic Typing issue allowing remote code execution.',
                            affectedVersions: '< 2.15.0',
                            fixedVersion: '2.15.0+',
                            reference: 'https://nvd.nist.gov/'
                        }
                    ]
                },
                {
                    groupId: 'org.apache.logging.log4j',
                    artifactId: 'log4j-core',
                    version: '2.14.1',
                    scope: 'compile',
                    category: 'compile',
                    vulnerabilityCount: 1,
                    riskLevel: 'CRITICAL',
                    vulnerabilities: [
                        {
                            cveId: 'CVE-2021-44228',
                            severity: 'CRITICAL',
                            cvssScore: 10.0,
                            description: 'Log4Shell - Remote Code Execution vulnerability in Log4j.',
                            affectedVersions: '2.0-beta9 to 2.14.1',
                            fixedVersion: '2.15.0',
                            reference: 'https://logging.apache.org/'
                        }
                    ]
                },
                {
                    groupId: 'com.mysql',
                    artifactId: 'mysql-connector-j',
                    version: '8.2.0',
                    scope: 'runtime',
                    category: 'runtime',
                    vulnerabilityCount: 0,
                    riskLevel: 'NONE',
                    vulnerabilities: []
                }
            ]
        };
    }
}
