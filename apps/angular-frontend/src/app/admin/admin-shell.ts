import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { BreakpointObserver } from '@angular/cdk/layout';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthApiService } from '../auth/auth-api.service';

@Component({
  selector: 'app-admin-shell',
  imports: [MatButtonModule, MatIconModule, MatListModule, MatSidenavModule, MatToolbarModule, RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <mat-toolbar color="primary" class="topbar">
      @if (isMobile()) { <button mat-icon-button type="button" aria-label="Ouvrir la navigation d’administration" (click)="toggleNavigation()"><mat-icon>menu</mat-icon></button> }
      <a class="brand" routerLink="/catalog">GamesUP</a>
      <span class="spacer"></span>
      <a mat-button routerLink="/catalog">Boutique</a>
      <button mat-button type="button" (click)="logout()"><mat-icon>logout</mat-icon> Déconnexion</button>
    </mat-toolbar>
    <mat-sidenav-container class="admin-layout">
      <mat-sidenav [mode]="isMobile() ? 'over' : 'side'" [opened]="navigationOpened()" class="admin-nav" aria-label="Navigation d’administration">
        <h2>Administration</h2>
        <mat-nav-list>
          <a mat-list-item routerLink="/admin/games" routerLinkActive="selected" ariaCurrentWhenActive="page" (click)="closeMobileNavigation()"><mat-icon matListItemIcon>sports_esports</mat-icon><span matListItemTitle>Jeux</span></a>
          <a mat-list-item routerLink="/admin/references" routerLinkActive="selected" ariaCurrentWhenActive="page" (click)="closeMobileNavigation()"><mat-icon matListItemIcon>collections_bookmark</mat-icon><span matListItemTitle>Auteurs & éditeurs</span></a>
          <a mat-list-item routerLink="/admin/users" routerLinkActive="selected" ariaCurrentWhenActive="page" (click)="closeMobileNavigation()"><mat-icon matListItemIcon>group</mat-icon><span matListItemTitle>Clients</span></a>
          <a mat-list-item routerLink="/admin/orders" routerLinkActive="selected" ariaCurrentWhenActive="page" (click)="closeMobileNavigation()"><mat-icon matListItemIcon>receipt_long</mat-icon><span matListItemTitle>Commandes</span></a>
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content class="admin-content"><router-outlet /></mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    :host { display:block; min-height:100vh; background:#f5f6fa; }
    .topbar { position:sticky; top:0; z-index:2; box-shadow:0 2px 8px #14213d22; }
    .brand { color:inherit; text-decoration:none; font-weight:800; }
    .spacer { flex:1; }
    .admin-layout { min-height:calc(100vh - 64px); }
    .admin-nav { width:250px; padding:1.25rem .75rem; border-right:1px solid #e2e6f0; }
    .admin-nav h2 { padding:0 1rem; color:#475569; font-size:.85rem; letter-spacing:.08em; text-transform:uppercase; }
    .admin-nav a { border-radius:10px; }
    .admin-nav a.selected { background:#e8eaf6; color:#303f9f; }
    .admin-content { padding:clamp(1rem,3vw,2.5rem); }
    @media (max-width:700px) { .admin-layout { min-height:calc(100vh - 56px); } .admin-nav { width:200px; } }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminShell implements OnInit {
  private readonly auth = inject(AuthApiService);
  private readonly router = inject(Router);
  private readonly breakpoints = inject(BreakpointObserver);
  readonly isMobile = signal(false);
  readonly navigationOpened = signal(true);
  ngOnInit(): void {
    this.breakpoints.observe('(max-width: 700px)').subscribe(({ matches }) => {
      this.isMobile.set(matches);
      this.navigationOpened.set(!matches);
    });
  }
  toggleNavigation(): void { this.navigationOpened.update((opened) => !opened); }
  closeMobileNavigation(): void { if (this.isMobile()) this.navigationOpened.set(false); }
  logout(): void { this.auth.logout(); void this.router.navigateByUrl('/login'); }
}
