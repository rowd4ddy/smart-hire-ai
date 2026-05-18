import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

type UserRole = 'CANDIDATE' | 'RECRUITER';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  type: string;
  email: string;
  fullName: string;
  role: UserRole;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  // Temporary developer bypass for previewing the protected frontend.
  // Set to false to restore normal login behavior.
  private readonly DEV_AUTH_BYPASS = true;
  private readonly DEV_AUTH_ROLE: UserRole = 'CANDIDATE';

  private readonly session = signal<AuthResponse | null>(this.readSession());

  login(payload: LoginPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap((response) => this.storeSession(response))
    );
  }

  register(payload: RegisterPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, payload).pipe(
      tap((response) => this.storeSession(response))
    );
  }

  logout(): void {
    localStorage.removeItem('smartHireAuth');
    this.session.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.session()?.token ?? null;
  }

  getRole(): UserRole | null {
    return this.session()?.role ?? null;
  }

  getFullName(): string {
    return this.session()?.fullName ?? '';
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getDashboardLink(): string {
    return this.getRole() === 'RECRUITER' ? '/recruiter' : '/candidate';
  }

  redirectByRole(): void {
    const role = this.getRole();

    if (role === 'RECRUITER') {
      this.router.navigate(['/recruiter']);
      return;
    }

    if (role === 'CANDIDATE') {
      this.router.navigate(['/candidate']);
      return;
    }

    this.router.navigate(['/login']);
  }

  private storeSession(response: AuthResponse): void {
    localStorage.setItem('smartHireAuth', JSON.stringify(response));
    this.session.set(response);
  }

  private readSession(): AuthResponse | null {
    const rawValue = localStorage.getItem('smartHireAuth');

    if (!rawValue) {
      return this.DEV_AUTH_BYPASS ? this.createDevSession() : null;
    }

    try {
      return JSON.parse(rawValue) as AuthResponse;
    } catch {
      localStorage.removeItem('smartHireAuth');
      return this.DEV_AUTH_BYPASS ? this.createDevSession() : null;
    }
  }

  private createDevSession(): AuthResponse {
    return {
      token: 'dev-bypass-token',
      type: 'Bearer',
      email: 'dev@smarthireai.local',
      fullName: 'Dev User',
      role: this.DEV_AUTH_ROLE
    };
  }
}
