import { CurrencyPipe, PercentPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthApiService } from '../auth/auth-api.service';
import { RecommendedGame, RecommendationResponse } from '../shared/api.models';
import { RecommendationApiService } from '../shared/recommendation-api.service';

@Component({
  selector: 'app-recommendations-page',
  imports: [CurrencyPipe, MatButtonModule, MatCardModule, MatIconModule, MatToolbarModule, PercentPipe, RouterLink],
  template: `
    <main class="recommendations-layout">
      <mat-toolbar color="primary" class="recommendations-header">
        <a class="brand" routerLink="/catalog">GamesUP</a><span class="spacer"></span>
        <a mat-button routerLink="/catalog">Catalogue</a>
        <a mat-button routerLink="/orders">Mes commandes</a>
        @if (auth.isAdmin()) { <a mat-button routerLink="/admin">Administration</a> }
        <button mat-button type="button" (click)="logout()"><mat-icon>logout</mat-icon> Déconnexion</button>
      </mat-toolbar>
      <section class="recommendations-content" aria-labelledby="recommendations-title">
        <p class="eyebrow">Sélection personnalisée</p>
        <h1 id="recommendations-title">Jeux recommandés</h1>
        <p class="intro">Les suggestions s’appuient d’abord sur les achats de clients aux goûts proches.</p>
        @if (loading()) {
          <p role="status">Calcul des recommandations…</p>
        } @else if (errorMessage()) {
          <p class="error-message" role="alert">{{ errorMessage() }}</p>
          <button mat-flat-button color="primary" type="button" (click)="loadRecommendations()">Réessayer</button>
        } @else if (recommendations().length === 0) {
          <p class="empty-state" role="status">Aucun jeu ne peut être recommandé pour le moment.</p>
          <a mat-button routerLink="/catalog">Parcourir le catalogue</a>
        } @else {
          @if (response()?.strategy === 'cold-start') {
            <p class="notice" role="status">Sélection basée sur les tendances générales, le temps que ton historique d’achat se forme.</p>
          }
          <ul class="recommendation-list">
            @for (item of recommendations(); track item.game.id) {
              <li><mat-card class="recommendation-card">
                <h2>{{ item.game.name }}</h2>
                <p>{{ item.game.genre }} · édition {{ item.game.editionNumber }}</p>
                @if (item.game.price !== null) { <p class="price">{{ item.game.price | currency:'EUR' }}</p> }
                <p class="score">Score de proximité : {{ item.score | percent:'1.0-0' }}</p>
                @if (item.game.author) { <p>Auteur : {{ item.game.author.name }}</p> }
                @if (item.game.publisher) { <p>Éditeur : {{ item.game.publisher.name }}</p> }
              </mat-card></li>
            }
          </ul>
        }
      </section>
    </main>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; }
    .recommendations-layout { min-height: 100vh; background: #f5f5f0; }
    .recommendations-header { display: flex; gap: 1rem; padding: 1rem clamp(1rem, 5vw, 4rem); background: #fff; border-bottom: 1px solid #e5e7df; }
    .brand { color: #145e59; font-weight: 800; text-decoration: none; }
    .spacer { flex: 1; }
    .recommendations-content { width: min(100% - 2rem, 70rem); margin: 0 auto; padding: clamp(2rem, 6vw, 4rem) 0; }
    .eyebrow { color: #14756c; font-size: .75rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
    h1 { margin: 0; color: #17211f; font-size: clamp(2rem, 6vw, 3rem); }
    .intro, .empty-state, .notice { color: #56625d; line-height: 1.6; }
    .notice { padding: 1rem; border-left: 4px solid #14756c; background: #fff; }
    .recommendation-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(min(100%, 17rem), 1fr)); gap: 1rem; padding: 0; list-style: none; }
    .recommendation-card { height: 100%; padding: 1.25rem; border: 1px solid #e5e7df; background: #fff; }
    .recommendation-card h2 { margin-top: 0; color: #17211f; }
    .price, .score { color: #145e59; font-weight: 700; }
    .error-message { color: #a32323; }
    a:focus-visible, button:focus-visible { outline: 3px solid #f4bd43; outline-offset: 3px; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecommendationsPage implements OnInit {
  private readonly recommendationsApi = inject(RecommendationApiService);
  private readonly router = inject(Router);
  readonly auth = inject(AuthApiService);

  readonly response = signal<RecommendationResponse | null>(null);
  readonly recommendations = signal<RecommendedGame[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    this.loadRecommendations();
  }

  loadRecommendations(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.recommendationsApi.getRecommendations().subscribe({
      next: (response) => {
        this.response.set(response);
        this.recommendations.set(response.recommendations);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Les recommandations ne sont pas disponibles pour le moment.');
        this.loading.set(false);
      },
    });
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
