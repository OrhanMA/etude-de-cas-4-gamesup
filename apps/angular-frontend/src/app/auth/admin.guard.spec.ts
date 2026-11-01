import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot } from '@angular/router';
import { AuthApiService } from './auth-api.service';
import { adminGuard } from './admin.guard';

function tokenFor(role: string): string {
  const payload = btoa(JSON.stringify({ roles: [role] }));
  return `header.${payload}.signature`;
}

describe('adminGuard', () => {
  let auth: AuthApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])] });
    auth = TestBed.inject(AuthApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('allows a token containing ROLE_ADMIN', () => {
    auth.login({ email: 'admin@example.test', password: 'secret123' }).subscribe();
    http.expectOne('/api/auth/login').flush({ accessToken: tokenFor('ROLE_ADMIN'), tokenType: 'Bearer' });

    const result = TestBed.runInInjectionContext(() => adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

    expect(result).toBe(true);
  });

  it('redirects clients to the catalogue', () => {
    auth.login({ email: 'client@example.test', password: 'secret123' }).subscribe();
    http.expectOne('/api/auth/login').flush({ accessToken: tokenFor('ROLE_USER'), tokenType: 'Bearer' });

    const result = TestBed.runInInjectionContext(() => adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

    expect(result).toEqual(TestBed.inject(Router).parseUrl('/catalog'));
  });
});
