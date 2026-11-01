import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AdminApiService } from '../shared/admin-api.service';
import { Game, Order, SaveOrderLine, UpdateOrderRequest } from '../shared/api.models';
import { CatalogApiService } from '../shared/catalog-api.service';
import { ConfirmDialog } from '../shared/confirm-dialog';

interface EditableLine { gameId: number; name: string; quantity: number; }

@Component({
  selector: 'app-orders-admin-page',
  imports: [CurrencyPipe, DatePipe, MatButtonModule, MatCardModule, MatCheckboxModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSnackBarModule, MatTableModule, ReactiveFormsModule],
  template: `
    <header class="page-heading"><div><p class="eyebrow">Administration</p><h1>Commandes</h1><p>Consulte les commandes, gère leur statut et corrige les lignes tant qu’elles ne sont ni payées ni livrées.</p></div></header>
    <mat-card class="table-card"><mat-card-header><mat-card-title>Toutes les commandes ({{ orders().length }})</mat-card-title></mat-card-header>
      @if (loading()) { <p class="state" role="status">Chargement des commandes…</p> }
      @else if (error()) { <p class="state error" role="alert">{{ error() }}</p> }
      @else if (orders().length === 0) { <p class="state">Aucune commande.</p> }
      @else { <div class="table-scroll"><table mat-table [dataSource]="orders()" aria-label="Toutes les commandes">
        <ng-container matColumnDef="id"><th mat-header-cell *matHeaderCellDef>Commande</th><td mat-cell *matCellDef="let order">#{{ order.id }}</td></ng-container>
        <ng-container matColumnDef="client"><th mat-header-cell *matHeaderCellDef>Client</th><td mat-cell *matCellDef="let order">{{ order.user.name }} · {{ order.user.email }}</td></ng-container>
        <ng-container matColumnDef="date"><th mat-header-cell *matHeaderCellDef>Date</th><td mat-cell *matCellDef="let order">{{ order.createdAt | date:'mediumDate' }}</td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Statut</th><td mat-cell *matCellDef="let order">{{ order.delivered ? 'Livrée' : order.paid ? 'Payée' : 'À payer' }}{{ order.archived ? ' · Archivée' : '' }}</td></ng-container>
        <ng-container matColumnDef="total"><th mat-header-cell *matHeaderCellDef>Total</th><td mat-cell *matCellDef="let order">{{ total(order) | currency:'EUR' }}</td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let order"><button mat-button type="button" (click)="selectOrder(order.id)">Détails / gérer</button><button mat-button color="warn" type="button" [disabled]="order.paid || order.delivered" (click)="remove(order)">Supprimer</button></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="columns"></tr><tr mat-row *matRowDef="let row; columns: columns"></tr>
      </table></div> }
    </mat-card>

    @if (selectedOrder(); as order) {
      <mat-card class="detail-card"><mat-card-header><mat-card-title>Commande #{{ order.id }} · {{ order.user.name }}</mat-card-title><mat-card-subtitle>{{ order.user.email }} · {{ order.createdAt | date:'medium' }}</mat-card-subtitle></mat-card-header>
        <mat-card-content>
          <h2>Statut de la commande</h2>
          <form [formGroup]="statusForm" (ngSubmit)="saveStatus()" class="status-form">
            <mat-checkbox formControlName="paid">Payée</mat-checkbox>
            <mat-checkbox formControlName="delivered">Livrée</mat-checkbox>
            <mat-checkbox formControlName="archived">Archivée</mat-checkbox>
            @if (statusForm.controls.delivered.value && !statusForm.controls.paid.value) { <p class="error" role="alert">Une commande doit être payée avant d’être livrée.</p> }
            <button mat-flat-button color="primary" type="submit" [disabled]="statusForm.invalid || (statusForm.controls.delivered.value && !statusForm.controls.paid.value) || saving()">Enregistrer le statut</button>
          </form>
          <h2>Articles ({{ lineTotal() | currency:'EUR' }})</h2>
          @if (order.paid || order.delivered) { <p class="state">Les articles ne peuvent plus être modifiés après paiement ou livraison.</p> }
          <ul class="line-list">@for (line of editableLines(); track line.gameId) { <li><span>{{ line.quantity }} × {{ line.name }}</span><div><strong>{{ linePrice(line, order) | currency:'EUR' }}</strong>@if (!order.paid && !order.delivered) { <button mat-button color="warn" type="button" (click)="removeLine(line.gameId)">Retirer</button> }</div></li> }</ul>
          @if (!order.paid && !order.delivered) {
            <form class="line-form" (submit)="$event.preventDefault(); addLine()">
              <mat-form-field appearance="outline"><mat-label>Ajouter un jeu</mat-label><mat-select [formControl]="newGameId"><mat-option [value]="0">Sélectionner…</mat-option>@for (game of games(); track game.id) { @if (game.price !== null) { <mat-option [value]="game.id">{{ game.name }} · {{ game.price | currency:'EUR' }}</mat-option> } }</mat-select></mat-form-field>
              <mat-form-field appearance="outline" class="qty"><mat-label>Quantité</mat-label><input matInput type="number" min="1" [formControl]="newQuantity" /></mat-form-field>
              <button mat-button type="submit" [disabled]="newGameId.value === 0 || newQuantity.invalid">Ajouter la ligne</button>
            </form>
            <button mat-flat-button color="primary" type="button" [disabled]="saving()" (click)="saveLines()">Enregistrer les articles</button>
          }
        </mat-card-content>
      </mat-card>
    }
  `,
  styles: [`:host{display:block}.page-heading{margin:0 0 1.5rem}.page-heading h1{margin:0;font-size:clamp(1.8rem,4vw,2.5rem)}.page-heading p{color:#586579}.eyebrow{color:#3949ab;font-size:.75rem;font-weight:700;letter-spacing:.1em;text-transform:uppercase}.table-card,.detail-card{margin-bottom:1.5rem;border-radius:16px}.table-scroll{overflow:auto}table{min-width:860px;width:100%}.state{padding:1rem 0;color:#56625d}.error{color:#b3261e}.status-form,.line-form{display:flex;flex-wrap:wrap;align-items:center;gap:1rem;padding:1rem 0}.line-form mat-form-field{min-width:250px}.line-form .qty{min-width:120px}.line-list{display:grid;gap:.5rem;padding:0;list-style:none}.line-list li,.line-list li>div{display:flex;align-items:center;justify-content:space-between;gap:1rem}.detail-card h2{margin:1.5rem 0 .25rem;font-size:1.1rem}`],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrdersAdminPage implements OnInit {
  private readonly api = inject(AdminApiService);
  private readonly catalog = inject(CatalogApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  readonly columns = ['id', 'client', 'date', 'status', 'total', 'actions'];
  readonly orders = signal<Order[]>([]);
  readonly selectedOrder = signal<Order | null>(null);
  readonly editableLines = signal<EditableLine[]>([]);
  readonly games = signal<Game[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly saving = signal(false);
  readonly newGameId = new FormControl(0, { nonNullable: true, validators: [Validators.min(1)] });
  readonly newQuantity = new FormControl(1, { nonNullable: true, validators: [Validators.required, Validators.min(1)] });
  readonly statusForm = new FormGroup({
    paid: new FormControl(false, { nonNullable: true }),
    delivered: new FormControl(false, { nonNullable: true }),
    archived: new FormControl(false, { nonNullable: true }),
  });
  readonly lineTotal = signal(0);

  ngOnInit(): void { this.load(); this.catalog.getGames().subscribe({ next: (games) => this.games.set(games), error: () => this.snack.open('Le catalogue des jeux est indisponible.', 'Fermer') }); }
  load(): void { this.loading.set(true); this.error.set(''); this.api.getOrders().subscribe({ next: (value) => { this.orders.set(value); this.loading.set(false); }, error: () => { this.error.set('Impossible de charger les commandes.'); this.loading.set(false); } }); }
  total(order: Order): number { return order.lines.reduce((sum, line) => sum + line.quantity * line.unitPrice, 0); }
  linePrice(line: EditableLine, order: Order): number { const previous = order.lines.find((entry) => entry.game.id === line.gameId); const current = this.games().find((game) => game.id === line.gameId); return (current?.price ?? previous?.unitPrice ?? 0) * line.quantity; }
  selectOrder(id: number): void { this.api.getOrder(id).subscribe({ next: (order) => { this.selectedOrder.set(order); this.statusForm.setValue({ paid: order.paid, delivered: order.delivered, archived: order.archived }); this.editableLines.set(order.lines.map((line) => ({ gameId: line.game.id, name: line.game.name, quantity: line.quantity }))); this.recalculate(order); }, error: () => this.snack.open('Impossible de charger cette commande.', 'Fermer') }); }
  addLine(): void { const id = this.newGameId.value; const game = this.games().find((entry) => entry.id === id); if (!game || game.price === null || this.newQuantity.invalid) return; const quantity = this.newQuantity.value; this.editableLines.update((lines) => { const found = lines.find((line) => line.gameId === id); return found ? lines.map((line) => line.gameId === id ? { ...line, quantity: line.quantity + quantity } : line) : [...lines, { gameId: id, name: game.name, quantity }]; }); const order = this.selectedOrder(); if (order) this.recalculate(order); this.newGameId.setValue(0); this.newQuantity.setValue(1); }
  removeLine(id: number): void { this.editableLines.update((lines) => lines.filter((line) => line.gameId !== id)); const order = this.selectedOrder(); if (order) this.recalculate(order); }
  private recalculate(order: Order): void { this.lineTotal.set(this.editableLines().reduce((sum, line) => sum + this.linePrice(line, order), 0)); }
  saveStatus(): void { const order = this.selectedOrder(); if (!order || this.statusForm.invalid) return; const value = this.statusForm.getRawValue(); if (value.delivered && !value.paid) return; this.saving.set(true); this.api.updateOrder(order.id, value).subscribe({ next: () => { this.snack.open('Statut mis à jour.', 'Fermer', { duration: 2300 }); this.saving.set(false); this.load(); this.selectOrder(order.id); }, error: () => { this.snack.open('Mise à jour du statut refusée par l’API.', 'Fermer'); this.saving.set(false); } }); }
  saveLines(): void { const order = this.selectedOrder(); if (!order || order.paid || order.delivered || this.saving()) return; const lines: SaveOrderLine[] = this.editableLines().map(({ gameId, quantity }) => ({ gameId, quantity })); const request: UpdateOrderRequest = { ...this.statusForm.getRawValue(), lines }; this.saving.set(true); this.api.updateOrder(order.id, request).subscribe({ next: () => { this.snack.open('Articles mis à jour.', 'Fermer', { duration: 2300 }); this.saving.set(false); this.load(); this.selectOrder(order.id); }, error: () => { this.snack.open('Les articles n’ont pas pu être modifiés.', 'Fermer'); this.saving.set(false); } }); }
  remove(order: Order): void { this.dialog.open(ConfirmDialog, { data: { title: `Supprimer la commande #${order.id} ?`, message: 'Cette action est permise uniquement pour une commande qui n’est ni payée ni livrée.' } }).afterClosed().subscribe((confirmed: boolean) => { if (!confirmed) return; this.api.deleteOrder(order.id).subscribe({ next: () => { this.snack.open('Commande supprimée.', 'Fermer', { duration: 2300 }); if (this.selectedOrder()?.id === order.id) this.selectedOrder.set(null); this.load(); }, error: () => this.snack.open('Suppression impossible pour cette commande.', 'Fermer') }); }); }
}
