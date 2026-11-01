import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { routes } from '../app.routes';
import { AuthApiService } from './auth-api.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTestingController: HttpTestingController;
  let auth: AuthApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter(routes),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthApiService);
  });

  afterEach(() => httpTestingController.verify());

  it('adds a Bearer header to API requests after login', () => {
    auth.login({ email: 'alice@example.test', password: 'secret123' }).subscribe();
    httpTestingController.expectOne('/api/auth/login').flush({
      accessToken: 'signed.jwt.token',
      tokenType: 'Bearer',
    });

    http.get('/games').subscribe();
    const request = httpTestingController.expectOne('/games');

    expect(request.request.headers.get('Authorization')).toBe('Bearer signed.jwt.token');
    request.flush([]);
  });

  it('does not attach authorization to requests outside the API', () => {
    http.get('/assets/logo.svg').subscribe();
    const request = httpTestingController.expectOne('/assets/logo.svg');

    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('clears an expired token and returns the user to login on a protected 401', () => {
    auth.login({ email: 'alice@example.test', password: 'secret123' }).subscribe();
    httpTestingController.expectOne('/api/auth/login').flush({
      accessToken: 'signed.jwt.token',
      tokenType: 'Bearer',
    });

    http.get('/games').subscribe({ error: () => undefined });
    httpTestingController.expectOne('/games').flush({}, {
      status: 401,
      statusText: 'Unauthorized',
    });

    expect(auth.isAuthenticated()).toBe(false);
  });
});
