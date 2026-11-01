import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthApiService } from '../auth/auth-api.service';
import { CreateOrderLine, OrdersApiService } from '../orders/orders-api.service';
import { CatalogApiService } from '../shared/catalog-api.service';
import { Game } from '../shared/api.models';

interface CartItem {
  game: Game & { price: number };
  quantity: number;
}

@Component({
  selector: 'app-catalog-page',
  imports: [CurrencyPipe, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatToolbarModule, ReactiveFormsModule, RouterLink],
  template: `
    <main class="catalog-layout">
      <mat-toolbar color="primary" class="catalog-header">
        <a class="brand" routerLink="/catalog">GamesUP</a><span class="toolbar-spacer"></span>
        @if (auth.isAdmin()) { <a mat-button routerLink="/admin">Administration</a> }
        <a mat-button routerLink="/orders">Mes commandes</a>
        <button mat-button type="button" (click)="logout()"><mat-icon>logout</mat-icon> Déconnexion</button>
      </mat-toolbar>
      <section class="catalog-content" aria-labelledby="catalog-title">
        <p class="eyebrow">Espace membre</p>
        <h1 id="catalog-title">Catalogue des jeux</h1>
        <p class="intro">Tu es connecté. Cette page charge le catalogue protégé par l’API.</p>
        <form class="search-form" (submit)="$event.preventDefault(); searchGames()">
          <mat-form-field appearance="outline" class="search-field">
            <mat-label>Rechercher par nom ou genre</mat-label>
            <input matInput [formControl]="searchQuery" />
            <mat-hint>La recherche utilise le catalogue de l’API.</mat-hint>
          </mat-form-field>
          <button mat-flat-button color="primary" type="submit" [disabled]="!searchQuery.value.trim()"><mat-icon>search</mat-icon> Rechercher</button>
          @if (searchActive()) { <button mat-button type="button" (click)="clearSearch()">Effacer</button> }
        </form>
        @if (checkoutMessage()) {
          <p class="success-message" role="status">{{ checkoutMessage() }}</p>
          <a routerLink="/orders">Consulter mes commandes</a>
        }

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
              <li><mat-card class="game-card">
                <h2>{{ game.name }}</h2>
                <p>{{ game.genre }} · édition {{ game.editionNumber }}</p>
                <p class="game-price">
                  @if (game.price !== null) { {{ game.price | currency:'EUR' }} }
                  @else { Prix à définir }
                </p>
                @if (game.author || game.publisher) {
                  <p class="game-meta">
                    @if (game.author) { Auteur : {{ game.author.name }} }
                    @if (game.author && game.publisher) { · }
                    @if (game.publisher) { Éditeur : {{ game.publisher.name }} }
                  </p>
                }
                <div class="game-actions">
                <button mat-button type="button" (click)="showDetails(game)">Détails</button>
                <button mat-flat-button color="primary" type="button" (click)="addToCart(game)"
                        [disabled]="game.price === null"
                        [attr.aria-label]="'Ajouter ' + game.name + ' au panier'">
                  {{ game.price === null ? 'Indisponible' : 'Ajouter au panier' }}
                </button>
                </div>
              </mat-card></li>
            }
          </ul>
        }

        <section class="cart-section" aria-labelledby="cart-title">
          <h2 id="cart-title">Mon panier <span class="cart-count">({{ cartCount() }} article(s))</span></h2>
          @if (cartItems().length === 0) {
            <p class="empty-state">Ton panier est vide.</p>
          } @else {
            <ul class="cart-list">
              @for (item of cartItems(); track item.game.id) {
                <li class="cart-item">
                  <span>{{ item.game.name }} · {{ item.game.price | currency:'EUR' }}</span>
                  <div class="quantity-controls" role="group"
                       [attr.aria-label]="'Quantité de ' + item.game.name">
                    <button type="button" (click)="decreaseQuantity(item.game.id)"
                            [attr.aria-label]="'Retirer un ' + item.game.name">−</button>
                    <span aria-live="polite">{{ item.quantity }}</span>
                    <button type="button" (click)="increaseQuantity(item.game.id)"
                            [attr.aria-label]="'Ajouter un ' + item.game.name">+</button>
                  </div>
                  <strong>{{ item.game.price * item.quantity | currency:'EUR' }}</strong>
                  <button class="remove-button" type="button" (click)="removeFromCart(item.game.id)"
                          [attr.aria-label]="'Supprimer ' + item.game.name + ' du panier'">Supprimer</button>
                </li>
              }
            </ul>
            <p class="cart-total"><span>Total estimé</span><strong>{{ cartTotal() | currency:'EUR' }}</strong></p>
            <p class="cart-note">Le total définitif est recalculé par l’API à partir des prix du catalogue.</p>
            @if (checkoutError()) { <p class="error-message" role="alert">{{ checkoutError() }}</p> }
            <button class="primary-button checkout-button" type="button" (click)="placeOrder()"
                    [disabled]="submitting()">
              {{ submitting() ? 'Création de la commande…' : 'Passer la commande' }}
            </button>
          }
        </section>
      </section>
    </main>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; }
    .catalog-layout { min-height: 100vh; background: #f5f5f0; }
    .catalog-header { display: flex; align-items: center; justify-content: space-between; padding: 1rem clamp(1rem, 5vw, 4rem); background: #fff; border-bottom: 1px solid #e5e7df; }
    .catalog-header nav { display: flex; align-items: center; gap: 1rem; }
    .catalog-header nav a { color: #145e59; font-weight: 700; }
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
    .game-price { color: #145e59 !important; font-size: 1.1rem; font-weight: 800; }
    .cart-section { margin-top: 3rem; padding: clamp(1rem, 4vw, 2rem); border: 1px solid #e5e7df; border-radius: 0.85rem; background: #fff; }
    .cart-section h2 { color: #17211f; }
    .cart-count, .cart-note { color: #56625d; font-size: 0.9rem; font-weight: 400; }
    .cart-list { display: grid; gap: 0.75rem; padding: 0; list-style: none; }
    .cart-item { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 0.75rem; padding: 0.75rem 0; border-bottom: 1px solid #e5e7df; }
    .quantity-controls { display: inline-flex; align-items: center; gap: 0.75rem; }
    .quantity-controls button, .remove-button { min-width: 2.25rem; min-height: 2.25rem; border: 1px solid #145e59; border-radius: 0.4rem; background: #fff; color: #145e59; font: inherit; font-weight: 700; cursor: pointer; }
    .remove-button { padding: 0.25rem 0.6rem; }
    .cart-total { display: flex; justify-content: space-between; margin: 1rem 0 0.25rem; font-size: 1.1rem; }
    .checkout-button { margin-top: 1rem; }
    .success-message { color: #145e59; font-weight: 700; }
    .primary-button:disabled { opacity: 0.7; cursor: wait; }
    .form-message { color: #a32323; }
    .primary-button { min-height: 2.8rem; padding: 0.5rem 1rem; border: 0; border-radius: 0.55rem; background: #145e59; color: #fff; font: inherit; font-weight: 700; cursor: pointer; }
    button:focus-visible, a:focus-visible { outline: 3px solid #f4bd43; outline-offset: 3px; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogPage implements OnInit {
  readonly auth = inject(AuthApiService);
  private readonly catalogApi = inject(CatalogApiService);
  private readonly ordersApi = inject(OrdersApiService);
  private readonly router = inject(Router);

  readonly games = signal<Game[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly cartItems = signal<CartItem[]>([]);
  readonly submitting = signal(false);
  readonly checkoutError = signal('');
  readonly checkoutMessage = signal('');
  readonly searchQuery = new FormControl('', { nonNullable: true });
  readonly searchActive = signal(false);
  readonly cartCount = computed(() => this.cartItems().reduce((count, item) => count + item.quantity, 0));
  readonly cartTotal = computed(() => this.cartItems().reduce(
    (total, item) => total + item.game.price * item.quantity,
    0,
  ));

  ngOnInit(): void {
    this.loadGames();
  }

  loadGames(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.searchActive.set(false);
    this.catalogApi.getGames().subscribe({
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

  searchGames(): void {
    const query = this.searchQuery.value.trim();
    if (!query) return;
    this.loading.set(true);
    this.errorMessage.set('');
    this.catalogApi.searchGames(query).subscribe({
      next: (games) => { this.games.set(games); this.loading.set(false); this.searchActive.set(true); },
      error: () => { this.errorMessage.set('La recherche n’a pas abouti. Vérifie le terme saisi puis réessaie.'); this.loading.set(false); },
    });
  }

  clearSearch(): void { this.searchQuery.setValue(''); this.loadGames(); }

  showDetails(game: Game): void { void this.router.navigate(['/catalog/game', game.id]); }

  addToCart(game: Game): void {
    if (game.price === null) return;
    const purchasableGame = { ...game, price: game.price };

    this.cartItems.update((items) => {
      const existing = items.find((item) => item.game.id === game.id);
      return existing
        ? items.map((item) => item.game.id === game.id ? { ...item, quantity: item.quantity + 1 } : item)
        : [...items, { game: purchasableGame, quantity: 1 }];
    });
    this.checkoutError.set('');
    this.checkoutMessage.set('');
  }

  increaseQuantity(gameId: number): void {
    this.cartItems.update((items) => items.map((item) =>
      item.game.id === gameId ? { ...item, quantity: item.quantity + 1 } : item,
    ));
  }

  decreaseQuantity(gameId: number): void {
    this.cartItems.update((items) => items
      .map((item) => item.game.id === gameId ? { ...item, quantity: item.quantity - 1 } : item)
      .filter((item) => item.quantity > 0));
  }

  removeFromCart(gameId: number): void {
    this.cartItems.update((items) => items.filter((item) => item.game.id !== gameId));
  }

  placeOrder(): void {
    if (this.cartItems().length === 0 || this.submitting()) return;

    const lines: CreateOrderLine[] = this.cartItems().map((item) => ({
      gameId: item.game.id,
      quantity: item.quantity,
    }));
    this.submitting.set(true);
    this.checkoutError.set('');
    this.checkoutMessage.set('');

    this.ordersApi.create(lines).subscribe({
      next: (order) => {
        this.cartItems.set([]);
        this.checkoutMessage.set(`Commande n° ${order.id} créée. Elle apparaîtra dans tes commandes.`);
        this.submitting.set(false);
      },
      error: () => {
        this.checkoutError.set('La commande n’a pas pu être créée. Vérifie ton panier puis réessaie.');
        this.submitting.set(false);
      },
    });
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
