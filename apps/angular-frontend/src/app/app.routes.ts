import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { adminGuard } from './auth/admin.guard';

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
    path: 'catalog/game/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./catalog/game-detail-page').then((module) => module.GameDetailPage),
  },
  {
    path: 'catalog',
    canActivate: [authGuard],
    loadComponent: () => import('./catalog/catalog-page').then((module) => module.CatalogPage),
  },
  {
    path: 'orders',
    canActivate: [authGuard],
    loadComponent: () => import('./orders/orders-page').then((module) => module.OrdersPage),
  },
  {
    path: 'recommendations',
    canActivate: [authGuard],
    loadComponent: () => import('./catalog/recommendations-page').then((module) => module.RecommendationsPage),
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./admin/admin-shell').then((module) => module.AdminShell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'games' },
      { path: 'games', loadComponent: () => import('./admin/games-admin-page').then((module) => module.GamesAdminPage) },
      { path: 'references', loadComponent: () => import('./admin/references-admin-page').then((module) => module.ReferencesAdminPage) },
      { path: 'users', loadComponent: () => import('./admin/users-admin-page').then((module) => module.UsersAdminPage) },
      { path: 'orders', loadComponent: () => import('./admin/orders-admin-page').then((module) => module.OrdersAdminPage) },
    ],
  },
  { path: '**', redirectTo: 'catalog' },
];
