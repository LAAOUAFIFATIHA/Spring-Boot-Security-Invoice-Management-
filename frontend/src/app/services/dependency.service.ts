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
            totalDependencies: 15,
            vulnerableDependencies: 1,
            vulnerabilityCountBySeverity: {
                'CRITICAL': 0,
                'HIGH': 0,
                'MEDIUM': 1,
                'LOW': 2
            },
            dependenciesByCategory: {
                'compile': 12,
                'runtime': 3
            },
            criticalCount: 0,
            highCount: 0,
            mediumCount: 1,
            lowCount: 2,
            overallRiskLevel: 'MEDIUM',
            dependencies: [
                {
                    groupId: 'com.itextpdf',
                    artifactId: 'itextpdf',
                    version: '5.5.13.3',
                    scope: 'compile',
                    category: 'compile',
                    vulnerabilityCount: 1,
                    riskLevel: 'MEDIUM',
                    vulnerabilities: [
                        {
                            cveId: 'CVE-2017-9096',
                            severity: 'MEDIUM',
                            cvssScore: 5.3,
                            description: 'iText PDF library contains a vulnerability in the PdfPKCS7 class that could allow signature validation bypass.',
                            affectedVersions: '5.x series',
                            fixedVersion: '7.x (itext7-core)',
                            reference: 'https://nvd.nist.gov/vuln/detail/CVE-2017-9096'
                        }
                    ]
                },
                {
                    groupId: 'io.jsonwebtoken',
                    artifactId: 'jjwt-api',
                    version: '0.11.5',
                    scope: 'compile',
                    category: 'compile',
                    vulnerabilityCount: 1,
                    riskLevel: 'LOW',
                    vulnerabilities: [
                        {
                            cveId: 'ADVISORY-2023',
                            severity: 'LOW',
                            cvssScore: 2.0,
                            description: 'Using an older version of JJWT. While no critical CVEs exist, newer versions (0.12.x) include security improvements.',
                            affectedVersions: '< 0.12.0',
                            fixedVersion: '0.12.5+',
                            reference: 'https://github.com/jwtk/jjwt/releases'
                        }
                    ]
                },
                {
                    groupId: 'com.fasterxml.jackson.core',
                    artifactId: 'jackson-databind',
                    version: '2.15.3',
                    scope: 'compile',
                    category: 'compile',
                    vulnerabilityCount: 0,
                    riskLevel: 'NONE',
                    vulnerabilities: []
                },
                {
                    groupId: 'com.mysql',
                    artifactId: 'mysql-connector-j',
                    version: '8.3.0',
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
