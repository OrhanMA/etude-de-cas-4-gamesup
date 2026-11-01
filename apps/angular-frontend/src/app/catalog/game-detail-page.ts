import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { CatalogApiService } from '../shared/catalog-api.service';
import { Game } from '../shared/api.models';

@Component({
  selector: 'app-game-detail-page',
  imports: [CurrencyPipe, MatButtonModule, MatCardModule, MatIconModule, RouterLink],
  template: `
    <main class="content">
      <a mat-button routerLink="/catalog"><mat-icon>arrow_back</mat-icon> Retour au catalogue</a>
      @if (loading()) { <p role="status">Chargement du jeu…</p> }
      @else if (error()) { <p role="alert">{{ error() }}</p> }
      @else if (game(); as current) {
        <mat-card>
          <mat-card-header><mat-card-title>{{ current.name }}</mat-card-title><mat-card-subtitle>{{ current.genre }} · édition {{ current.editionNumber }}</mat-card-subtitle></mat-card-header>
          <mat-card-content>
            <p class="price">{{ current.price === null ? 'Prix à définir' : (current.price | currency:'EUR') }}</p>
            <p>Auteur : {{ current.author?.name ?? 'Non renseigné' }}</p>
            <p>Éditeur : {{ current.publisher?.name ?? 'Non renseigné' }}</p>
          </mat-card-content>
        </mat-card>
      }
    </main>
  `,
  styles: [`.content{max-width:760px;margin:2rem auto;padding:1rem}.price{font-size:1.5rem;font-weight:700;color:#303f9f}`],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly catalog = inject(CatalogApiService);
  readonly game = signal<Game | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id < 1) { this.error.set('Identifiant de jeu invalide.'); this.loading.set(false); return; }
    this.catalog.getGame(id).subscribe({
      next: (game) => { this.game.set(game); this.loading.set(false); },
      error: () => { this.error.set('Ce jeu est introuvable ou le service est indisponible.'); this.loading.set(false); },
    });
  }
}
