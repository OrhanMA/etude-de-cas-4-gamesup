import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { Author, Game, Publisher, SaveGameRequest } from '../shared/api.models';
import { CatalogApiService } from '../shared/catalog-api.service';
import { ConfirmDialog } from '../shared/confirm-dialog';

@Component({
  selector: 'app-games-admin-page',
  imports: [CurrencyPipe, MatButtonModule, MatCardModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSnackBarModule, MatTableModule, ReactiveFormsModule],
  template: `
    <header class="page-heading"><div><p class="eyebrow">Catalogue</p><h1>Gestion des jeux</h1><p>Créer, consulter, modifier ou retirer les jeux du catalogue.</p></div></header>
    <mat-card class="editor">
      <mat-card-header><mat-card-title>{{ editingId() ? 'Modifier un jeu' : 'Ajouter un jeu' }}</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="save()" class="form-grid">
          <mat-form-field appearance="outline"><mat-label>Nom</mat-label><input matInput formControlName="name" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Genre</mat-label><input matInput formControlName="genre" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Numéro d’édition</mat-label><input matInput type="number" min="1" formControlName="editionNumber" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Prix (€)</mat-label><input matInput type="number" min="0" step="0.01" formControlName="price" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Auteur</mat-label><mat-select formControlName="authorId"><mat-option [value]="''">Non renseigné</mat-option>@for (author of authors(); track author.id) { <mat-option [value]="author.id.toString()">{{ author.name }}</mat-option> }</mat-select></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Éditeur</mat-label><mat-select formControlName="publisherId"><mat-option [value]="''">Non renseigné</mat-option>@for (publisher of publishers(); track publisher.id) { <mat-option [value]="publisher.id.toString()">{{ publisher.name }}</mat-option> }</mat-select></mat-form-field>
          <div class="form-actions"><button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">{{ saving() ? 'Enregistrement…' : editingId() ? 'Enregistrer' : 'Créer le jeu' }}</button>@if (editingId()) { <button mat-button type="button" (click)="resetForm()">Annuler</button> }</div>
        </form>
      </mat-card-content>
    </mat-card>

    <mat-card class="table-card">
      <mat-card-header><mat-card-title>Catalogue ({{ games().length }})</mat-card-title></mat-card-header>
      @if (loading()) { <p class="state" role="status">Chargement des jeux…</p> }
      @else if (error()) { <p class="state error" role="alert">{{ error() }}</p> }
      @else if (games().length === 0) { <p class="state">Aucun jeu pour le moment.</p> }
      @else {
        <div class="table-scroll"><table mat-table [dataSource]="games()" aria-label="Jeux du catalogue">
          <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Jeu</th><td mat-cell *matCellDef="let game">{{ game.name }}</td></ng-container>
          <ng-container matColumnDef="genre"><th mat-header-cell *matHeaderCellDef>Genre</th><td mat-cell *matCellDef="let game">{{ game.genre }} · éd. {{ game.editionNumber }}</td></ng-container>
          <ng-container matColumnDef="price"><th mat-header-cell *matHeaderCellDef>Prix</th><td mat-cell *matCellDef="let game">{{ game.price === null ? 'À définir' : (game.price | currency:'EUR') }}</td></ng-container>
          <ng-container matColumnDef="references"><th mat-header-cell *matHeaderCellDef>Auteur / éditeur</th><td mat-cell *matCellDef="let game">{{ game.author?.name ?? '—' }} / {{ game.publisher?.name ?? '—' }}</td></ng-container>
          <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let game"><button mat-button type="button" [attr.aria-label]="'Modifier le jeu ' + game.name" (click)="edit(game.id)">Modifier</button><button mat-button color="warn" type="button" [attr.aria-label]="'Supprimer le jeu ' + game.name" (click)="remove(game)">Supprimer</button></td></ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr><tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table></div>
      }
    </mat-card>
  `,
  styles: [`
    :host{display:block}.page-heading{margin:0 0 1.5rem}.page-heading h1{margin:0;font-size:clamp(1.8rem,4vw,2.5rem)}.page-heading p{color:#586579}.eyebrow{color:#3949ab;font-size:.75rem;font-weight:700;letter-spacing:.1em;text-transform:uppercase}.editor,.table-card{margin-bottom:1.5rem;border-radius:16px}.form-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:0 1rem;padding-top:1rem}.form-actions{display:flex;align-items:start;gap:.5rem}.table-scroll{overflow:auto}table{min-width:700px;width:100%}.state{padding:1.5rem;color:#56625d}.error{color:#b3261e}@media(max-width:800px){.form-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:520px){.form-grid{grid-template-columns:1fr}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GamesAdminPage implements OnInit {
  private readonly api = inject(CatalogApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  readonly columns = ['name', 'genre', 'price', 'references', 'actions'];
  readonly games = signal<Game[]>([]);
  readonly authors = signal<Author[]>([]);
  readonly publishers = signal<Publisher[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly editingId = signal<number | null>(null);
  readonly form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    genre: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    editionNumber: new FormControl(1, { nonNullable: true, validators: [Validators.required, Validators.min(1)] }),
    price: new FormControl(0, { nonNullable: true, validators: [Validators.required, Validators.min(0)] }),
    authorId: new FormControl('', { nonNullable: true }),
    publisherId: new FormControl('', { nonNullable: true }),
  });

  ngOnInit(): void { this.load(); this.api.getAuthors().subscribe({ next: (value) => this.authors.set(value), error: () => this.snack.open('Les auteurs sont indisponibles.', 'Fermer') }); this.api.getPublishers().subscribe({ next: (value) => this.publishers.set(value), error: () => this.snack.open('Les éditeurs sont indisponibles.', 'Fermer') }); }
  load(): void { this.loading.set(true); this.error.set(''); this.api.getGames().subscribe({ next: (value) => { this.games.set(value); this.loading.set(false); }, error: () => { this.error.set('Impossible de charger les jeux.'); this.loading.set(false); } }); }
  edit(id: number): void { this.api.getGame(id).subscribe({ next: (game) => { this.editingId.set(id); this.form.setValue({ name: game.name, genre: game.genre, editionNumber: game.editionNumber, price: game.price ?? 0, authorId: game.author?.id.toString() ?? '', publisherId: game.publisher?.id.toString() ?? '' }); }, error: () => this.snack.open('Le jeu sélectionné est introuvable.', 'Fermer') }); }
  save(): void {
    this.form.markAllAsTouched(); if (this.form.invalid || this.saving()) return;
    const value = this.form.getRawValue();
    const request: SaveGameRequest = { name: value.name.trim(), genre: value.genre.trim(), editionNumber: Number(value.editionNumber), price: Number(value.price), authorId: value.authorId ? Number(value.authorId) : null, publisherId: value.publisherId ? Number(value.publisherId) : null };
    this.saving.set(true);
    const request$ = this.editingId() ? this.api.updateGame(this.editingId()!, request) : this.api.createGame(request);
    request$.subscribe({ next: () => { this.snack.open(this.editingId() ? 'Jeu modifié.' : 'Jeu créé.', 'Fermer', { duration: 2500 }); this.resetForm(); this.load(); this.saving.set(false); }, error: () => { this.snack.open('Enregistrement impossible. Vérifie les références et les champs.', 'Fermer'); this.saving.set(false); } });
  }
  resetForm(): void { this.editingId.set(null); this.form.reset({ name: '', genre: '', editionNumber: 1, price: 0, authorId: '', publisherId: '' }); }
  remove(game: Game): void { this.dialog.open(ConfirmDialog, { data: { title: 'Supprimer ce jeu ?', message: `« ${game.name} » sera retiré du catalogue.` } }).afterClosed().subscribe((confirmed: boolean) => { if (!confirmed) return; this.api.deleteGame(game.id).subscribe({ next: () => { this.snack.open('Jeu supprimé.', 'Fermer', { duration: 2500 }); this.load(); }, error: () => this.snack.open('Suppression impossible. Le jeu est peut-être utilisé par une commande.', 'Fermer') }); }); }
}
