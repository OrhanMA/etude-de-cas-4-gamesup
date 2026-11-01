import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'catalog' },
  {
    path: 'login',
    loadComponent: () => import('./auth/login-page').then((module) => module.LoginPage),
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register-page').then((module) => module.RegisterPage),
  },
  {
    path: 'catalog',
    canActivate: [authGuard],
    loadComponent: () => import('./catalog/catalog-page').then((module) => module.CatalogPage),
  },
  { path: '**', redirectTo: 'catalog' },
];
