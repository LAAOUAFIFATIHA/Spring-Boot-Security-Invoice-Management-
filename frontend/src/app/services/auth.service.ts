import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, RegisterRequest } from '../models/user';

@Injectable({
  providedIn: 'root'
})

export class AuthService {

  private apiUrl = 'http://localhost:8090/api/auth';
  private tokenKey = 'token'; // Legacy key, still used for compatibility
  private accessTokenKey = 'accessToken'; // New enhanced security
  private refreshTokenKey = 'refreshToken'; // New enhanced security
  private usernameKey = 'username';
  private roleKey = 'role';
  private clientIdKey = 'id_client';

  constructor(private http: HttpClient) { }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        // Support both new enhanced format and legacy format
        if (response.accessToken) {
          // New enhanced security format
          localStorage.setItem(this.accessTokenKey, response.accessToken);
          localStorage.setItem(this.tokenKey, response.accessToken); // Also store in legacy key for compatibility
          if (response.refreshToken) {
            localStorage.setItem(this.refreshTokenKey, response.refreshToken);
          }
        } else if (response.token) {
          // Legacy format (backward compatibility)
          localStorage.setItem(this.tokenKey, response.token);
        }

        // Store user info
        localStorage.setItem(this.usernameKey, response.username);
        localStorage.setItem(this.roleKey, response.role);
        if (response.id_client) {
          localStorage.setItem(this.clientIdKey, response.id_client.toString());
        }
      })
    );
  }

  register(request: RegisterRequest, role: string): Observable<any> {
    const endpoint = `${this.apiUrl}/register/${role.toLowerCase()}`;
    return this.http.post(endpoint, request);
  }

  logout(): void {
    // Call backend logout endpoint to blacklist token
    const token = this.getToken();
    if (token) {
      this.http.post(`${this.apiUrl}/logout`, {}).subscribe({
        next: () => console.log('Logged out from backend'),
        error: (err) => console.warn('Backend logout failed', err)
      });
    }

    // Clear all local storage
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.accessTokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    localStorage.removeItem(this.usernameKey);
    localStorage.removeItem(this.roleKey);
    localStorage.removeItem(this.clientIdKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUsername(): string | null {
    return localStorage.getItem(this.usernameKey);
  }

  getRole(): string | null {
    return localStorage.getItem(this.roleKey);
  }

  getClientId(): number | null {
    const id = localStorage.getItem(this.clientIdKey);
    return id ? parseInt(id, 10) : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasRole(role: string): boolean {
    return this.getRole() === role;
  }

  hasAnyRole(roles: string[]): boolean {
    const userRole = this.getRole();
    return userRole !== null && roles.includes(userRole);
  }
}
