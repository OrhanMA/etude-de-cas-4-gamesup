import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthApiService } from './auth-api.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthApiService);
  const router = inject(Router);
  return auth.isAdmin() ? true : router.createUrlTree(['/catalog']);
};
