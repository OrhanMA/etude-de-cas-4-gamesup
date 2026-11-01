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
}
