import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest extends LoginRequest {
  name: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly tokenState = signal<string | null>(null);

  readonly accessToken = this.tokenState.asReadonly();

  login(credentials: LoginRequest) {
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap((response) => this.tokenState.set(response.accessToken)),
    );
  }

  register(request: RegisterRequest) {
    return this.http.post<void>('/api/auth/register', request);
  }

  logout(): void {
    this.tokenState.set(null);
  }

  isAuthenticated(): boolean {
    return this.tokenState() !== null;
  }

  isAdmin(): boolean {
    const token = this.tokenState();
    if (!token) return false;

    try {
      const encodedPayload = token.split('.')[1];
      if (!encodedPayload) return false;
      const base64 = encodedPayload.replace(/-/g, '+').replace(/_/g, '/');
      const paddedBase64 = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
      const payload = JSON.parse(atob(paddedBase64)) as { roles?: unknown };
      return Array.isArray(payload.roles) && payload.roles.some((role: unknown) => role === 'ROLE_ADMIN');
    } catch {
      return false;
    }
  }
}
