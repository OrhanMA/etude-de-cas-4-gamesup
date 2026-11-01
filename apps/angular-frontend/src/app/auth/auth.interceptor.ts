import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthApiService } from './auth-api.service';

function isApiRequest(url: string): boolean {
  return url.startsWith('/api/') || url === '/games' || url.startsWith('/games/');
}

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthApiService);
  const router = inject(Router);
  const token = auth.accessToken();
  const authorizedRequest = token && isApiRequest(request.url)
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authorizedRequest).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && auth.isAuthenticated()) {
        auth.logout();
        void router.navigateByUrl('/login');
      }

      return throwError(() => error);
    }),
  );
};
