import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserRiskProfile {
    username: string;
    riskScore: number;
    riskLevel: string;
    incidentCount: number;
    abnormalInvoiceCount: number;
    topRiskFactor: string;
    recommendations: string[];
}

export interface InvoiceStats {
    totalInvoices: number;
    highValueFlagged: number;
    suspiciousTransactions: any[];
}

export interface VulnerabilityReport {
    component: string;
    cveId: string;
    severity: string;
    description: string;
    remediation: string;
}

@Injectable({
    providedIn: 'root'
})
export class SecurityService {
    private apiUrl = 'http://localhost:8090/api/admin/security';

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders().set('Authorization', `Bearer ${token}`);
    }

    getRiskyUsers(limit: number = 10): Observable<UserRiskProfile[]> {
        return this.http.get<UserRiskProfile[]>(`${this.apiUrl}/risky-users?limit=${limit}`, { headers: this.getHeaders() });
    }

    getInvoiceStats(): Observable<InvoiceStats> {
        return this.http.get<InvoiceStats>(`${this.apiUrl}/invoice-stats`, { headers: this.getHeaders() });
    }

    getVulnerabilities(): Observable<VulnerabilityReport[]> {
        return this.http.get<VulnerabilityReport[]>(`${this.apiUrl}/vulnerabilities`, { headers: this.getHeaders() });
    }
}
