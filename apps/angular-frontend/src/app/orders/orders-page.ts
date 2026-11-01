import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../auth/auth-api.service';
import { CustomerOrder, OrdersApiService } from './orders-api.service';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-orders-page',
  imports: [CurrencyPipe, DatePipe, MatButtonModule, MatCardModule, MatIconModule, MatToolbarModule, RouterLink],
  template: `
    <main class="orders-layout">
      <mat-toolbar color="primary" class="orders-header"><a class="brand" routerLink="/catalog">GamesUP</a><span class="spacer"></span>
        @if (auth.isAdmin()) { <a mat-button routerLink="/admin">Administration</a> }
        <a mat-button routerLink="/catalog">Catalogue</a><button mat-button type="button" (click)="logout()"><mat-icon>logout</mat-icon> Déconnexion</button>
      </mat-toolbar>

      <section class="orders-content" aria-labelledby="orders-title">
        <p class="eyebrow">Espace membre</p>
        <h1 id="orders-title">Mes commandes</h1>
        <p class="intro">Retrouve ici les commandes associées à ton compte.</p>

        @if (loading()) {
          <p role="status">Chargement de tes commandes…</p>
        } @else if (errorMessage()) {
          <p class="error-message" role="alert">{{ errorMessage() }}</p>
          <button class="primary-button" type="button" (click)="loadOrders()">Réessayer</button>
        } @else if (orders().length === 0) {
          <div class="empty-state">
            <p>Tu n’as pas encore passé de commande.</p>
            <a class="primary-link" routerLink="/catalog">Découvrir le catalogue</a>
          </div>
        } @else {
          <ul class="order-list">
            @for (order of orders(); track order.id) {
              <li><mat-card class="order-card">
                <div class="order-heading">
                  <h2>Commande #{{ order.id }}</h2>
                  <time [attr.datetime]="order.createdAt">{{ order.createdAt | date:'mediumDate' }}</time>
                </div>
                <p class="order-status">
                  {{ order.delivered ? 'Livrée' : order.paid ? 'Payée' : 'En attente de paiement' }}
                </p>
                <ul class="line-list" aria-label="Articles de la commande">
                  @for (line of order.lines; track line.id) {
                    <li>
                      <span>{{ line.quantity }} × {{ line.game.name }}</span>
                      <span>{{ line.unitPrice * line.quantity | currency:'EUR' }}</span>
                    </li>
                  }
                </ul>
                <p class="order-total">Total <strong>{{ orderTotal(order) | currency:'EUR' }}</strong></p>
                <button mat-button type="button" (click)="showOrderDetails(order.id)">Charger le détail de cette commande</button>
                @if (selectedOrder()?.id === order.id) { <p class="detail-confirmation" role="status">Détail récupéré depuis l’API pour la commande #{{ selectedOrder()?.id }} · {{ selectedOrder()?.lines?.length }} ligne(s).</p> }
              </mat-card></li>
            }
          </ul>
        }
      </section>
    </main>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; }
    .orders-layout { min-height: 100vh; background: #f5f5f0; }
    .orders-header { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 1rem clamp(1rem, 5vw, 4rem); background: #fff; border-bottom: 1px solid #e5e7df; }
    .brand { color: #145e59; font-weight: 800; text-decoration: none; }
    nav { display: flex; align-items: center; gap: 1rem; }
    nav a { color: #145e59; font-weight: 700; }
    .text-button { min-height: 2.5rem; padding: 0.4rem 0.8rem; border: 1px solid #145e59; border-radius: 0.5rem; background: #fff; color: #145e59; font: inherit; font-weight: 700; cursor: pointer; }
    .orders-content { width: min(100% - 2rem, 70rem); margin: 0 auto; padding: clamp(2rem, 6vw, 4rem) 0; }
    .eyebrow { color: #14756c; font-size: 0.75rem; font-weight: 800; letter-spacing: 0.12em; text-transform: uppercase; }
    h1 { margin: 0; color: #17211f; font-size: clamp(2rem, 6vw, 3rem); letter-spacing: -0.04em; }
    .intro { color: #56625d; line-height: 1.6; }
    .order-list { display: grid; gap: 1rem; padding: 0; list-style: none; }
    .order-card, .empty-state { padding: 1.25rem; border: 1px solid #e5e7df; border-radius: 0.85rem; background: #fff; }
    .order-heading { display: flex; justify-content: space-between; gap: 1rem; }
    .order-heading h2 { margin: 0; color: #17211f; font-size: 1.2rem; }
    .order-heading time, .order-status { color: #56625d; }
    .line-list { display: grid; gap: 0.5rem; padding: 0; list-style: none; }
    .line-list li { display: flex; justify-content: space-between; gap: 1rem; }
    .order-total { display: flex; justify-content: space-between; padding-top: 0.75rem; border-top: 1px solid #e5e7df; }
    .empty-state { color: #56625d; }
    .primary-link, .primary-button { display: inline-block; min-height: 2.8rem; padding: 0.7rem 1rem; border: 0; border-radius: 0.55rem; background: #145e59; color: #fff; font: inherit; font-weight: 700; text-decoration: none; cursor: pointer; }
    .error-message { color: #a32323; }
    button:focus-visible, a:focus-visible { outline: 3px solid #f4bd43; outline-offset: 3px; }
    @media (max-width: 32rem) { .order-heading { flex-direction: column; gap: 0.25rem; } }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrdersPage implements OnInit {
  private readonly ordersApi = inject(OrdersApiService);
  readonly auth = inject(AuthApiService);
  private readonly router = inject(Router);

  readonly orders = signal<CustomerOrder[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly selectedOrder = signal<CustomerOrder | null>(null);

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.ordersApi.getMine().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Tes commandes n’ont pas pu être chargées.');
        this.loading.set(false);
      },
    });
  }

  orderTotal(order: CustomerOrder): number {
    return order.lines.reduce((total, line) => total + line.unitPrice * line.quantity, 0);
  }

  showOrderDetails(id: number): void {
    this.ordersApi.getMineById(id).subscribe({
      next: (order) => this.selectedOrder.set(order),
      error: () => this.errorMessage.set('Le détail de cette commande est indisponible.'),
    });
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
