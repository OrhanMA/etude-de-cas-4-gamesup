import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot } from '@angular/router';
import { AuthApiService } from './auth-api.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  it('redirects anonymous visitors to login', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

    expect(result).toEqual(TestBed.inject(Router).parseUrl('/login'));
  });

  it('allows a user with an in-memory token through', () => {
    const auth = TestBed.inject(AuthApiService);
    const httpTestingController = TestBed.inject(HttpTestingController);

    auth.login({ email: 'alice@example.test', password: 'secret123' }).subscribe();
    httpTestingController.expectOne('/api/auth/login').flush({
      accessToken: 'signed.jwt.token',
      tokenType: 'Bearer',
    });

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

    expect(result).toBe(true);
    httpTestingController.verify();
  });
});
