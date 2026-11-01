import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthApiService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('sends login credentials and keeps the returned access token in memory', () => {
    service.login({ email: 'alice@example.test', password: 'secret123' }).subscribe();

    const request = httpTestingController.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ email: 'alice@example.test', password: 'secret123' });

    request.flush({ accessToken: 'signed.jwt.token', tokenType: 'Bearer' });

    expect(service.accessToken()).toBe('signed.jwt.token');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('registers a user without treating registration as a login', () => {
    service.register({ name: 'Alice', email: 'alice@example.test', password: 'secret123' }).subscribe();

    const request = httpTestingController.expectOne('/api/auth/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      name: 'Alice',
      email: 'alice@example.test',
      password: 'secret123',
    });
    request.flush(null);

    expect(service.isAuthenticated()).toBe(false);
  });

  it('clears the in-memory token when signing out', () => {
    service.login({ email: 'alice@example.test', password: 'secret123' }).subscribe();
    httpTestingController.expectOne('/api/auth/login').flush({
      accessToken: 'signed.jwt.token',
      tokenType: 'Bearer',
    });

    service.logout();

    expect(service.accessToken()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });
});
