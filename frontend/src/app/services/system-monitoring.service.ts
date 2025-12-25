import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SystemMetrics {
    jvm_memory_used: number;
    jvm_memory_max: number;
    jvm_threads_live: number;
    process_cpu_usage: number;
    system_cpu_usage: number;
    uptime: number;
}

export interface PortStatus {
    port: number;
    isOpen: boolean;
    service: string;
}

@Injectable({
    providedIn: 'root'
})
export class SystemMonitoringService {
    private apiUrl = 'http://localhost:8090/api/admin/system';

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders().set('Authorization', `Bearer ${token}`);
    }

    getMetrics(): Observable<SystemMetrics> {
        return this.http.get<SystemMetrics>(`${this.apiUrl}/metrics`, { headers: this.getHeaders() });
    }

    getHealth(): Observable<any> {
        return this.http.get<any>('http://localhost:8090/actuator/health', { headers: this.getHeaders() });
    }

    getInfo(): Observable<any> {
        return this.http.get<any>('http://localhost:8090/actuator/info', { headers: this.getHeaders() });
    }

    getEnv(): Observable<any> {
        return this.http.get<any>('http://localhost:8090/actuator/env', { headers: this.getHeaders() });
    }

    getBeans(): Observable<any> {
        return this.http.get<any>('http://localhost:8090/actuator/beans', { headers: this.getHeaders() });
    }

    getMetricsList(): Observable<any> {
        return this.http.get<any>('http://localhost:8090/actuator/metrics', { headers: this.getHeaders() });
    }

    scanPorts(start: number, end: number): Observable<PortStatus[]> {
        const params = new HttpParams()
            .set('start', start.toString())
            .set('end', end.toString());
        return this.http.get<PortStatus[]>(`${this.apiUrl}/scan-ports`, {
            headers: this.getHeaders(),
            params: params
        });
    }
}
