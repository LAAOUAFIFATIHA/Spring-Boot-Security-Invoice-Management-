import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardStatsDto } from '../models/dashboard';

@Injectable({
    providedIn: 'root'
})
export class DashboardService {
    private apiUrl = 'http://localhost:8090/api/dashboard';

    constructor(private http: HttpClient) { }

    getAdminStats(): Observable<DashboardStatsDto> {
        return this.http.get<DashboardStatsDto>(`${this.apiUrl}/admin`);
    }
}
