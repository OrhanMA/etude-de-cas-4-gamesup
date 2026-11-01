import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../auth/auth-api.service';

interface Game {
  id: number;
  name: string;
  genre: string;
  editionNumber: number;
  author: { id: number; name: string } | null;
  publisher: { id: number; name: string } | null;
}

@Component({
  selector: 'app-catalog-page',
  imports: [RouterLink],
  template: `
    <main class="catalog-layout">
      <header class="catalog-header">
        <a class="brand" routerLink="/catalog">GamesUP</a>
        <button class="text-button" type="button" (click)="logout()">Se déconnecter</button>
      </header>
      <section class="catalog-content" aria-labelledby="catalog-title">
        <p class="eyebrow">Espace membre</p>
        <h1 id="catalog-title">Catalogue des jeux</h1>
        <p class="intro">Tu es connecté. Cette page charge le catalogue protégé par l’API.</p>

        @if (loading()) {
          <p role="status">Chargement du catalogue…</p>
        } @else if (errorMessage()) {
          <p class="form-message error-message" role="alert">{{ errorMessage() }}</p>
          <button class="primary-button" type="button" (click)="loadGames()">Réessayer</button>
        } @else if (games().length === 0) {
          <p class="empty-state">Le catalogue est vide pour le moment.</p>
        } @else {
          <ul class="game-list">
            @for (game of games(); track game.id) {
              <li class="game-card">
                <h2>{{ game.name }}</h2>
                <p>{{ game.genre }} · édition {{ game.editionNumber }}</p>
                @if (game.author || game.publisher) {
                  <p class="game-meta">
                    @if (game.author) { Auteur : {{ game.author.name }} }
                    @if (game.author && game.publisher) { · }
                    @if (game.publisher) { Éditeur : {{ game.publisher.name }} }
                  </p>
                }
              </li>
            }
          </ul>
        }
      </section>
    </main>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; }
    .catalog-layout { min-height: 100vh; background: #f5f5f0; }
    .catalog-header { display: flex; align-items: center; justify-content: space-between; padding: 1rem clamp(1rem, 5vw, 4rem); background: #fff; border-bottom: 1px solid #e5e7df; }
    .brand { color: #145e59; font-weight: 800; text-decoration: none; }
    .text-button { min-height: 2.5rem; padding: 0.4rem 0.8rem; border: 1px solid #145e59; border-radius: 0.5rem; background: #fff; color: #145e59; font: inherit; font-weight: 700; cursor: pointer; }
    .catalog-content { width: min(100% - 2rem, 70rem); margin: 0 auto; padding: clamp(2rem, 6vw, 4rem) 0; }
    .eyebrow { color: #14756c; font-size: 0.75rem; font-weight: 800; letter-spacing: 0.12em; text-transform: uppercase; }
    h1 { margin: 0; color: #17211f; font-size: clamp(2rem, 6vw, 3rem); letter-spacing: -0.04em; }
    .intro { color: #56625d; line-height: 1.6; }
    .game-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(min(100%, 17rem), 1fr)); gap: 1rem; padding: 0; list-style: none; }
    .game-card, .empty-state { padding: 1.25rem; border: 1px solid #e5e7df; border-radius: 0.85rem; background: #fff; }
    .game-card h2 { margin: 0 0 0.5rem; color: #17211f; font-size: 1.2rem; }
    .game-card p, .empty-state { color: #56625d; }
    .game-meta { font-size: 0.875rem; }
    .form-message { color: #a32323; }
    .primary-button { min-height: 2.8rem; padding: 0.5rem 1rem; border: 0; border-radius: 0.55rem; background: #145e59; color: #fff; font: inherit; font-weight: 700; cursor: pointer; }
    button:focus-visible, a:focus-visible { outline: 3px solid #f4bd43; outline-offset: 3px; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogPage implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthApiService);
  private readonly router = inject(Router);

  readonly games = signal<Game[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    this.loadGames();
  }

  loadGames(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.http.get<Game[]>('/games').subscribe({
      next: (games) => {
        this.games.set(games);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        const status = typeof error === 'object' && error !== null && 'status' in error
          ? error.status
          : undefined;
        this.errorMessage.set(status === 0
          ? 'Impossible de joindre l’API. Vérifie qu’elle est démarrée.'
          : 'Le catalogue n’a pas pu être chargé.');
        this.loading.set(false);
      },
    });
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
