import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { AdminApiService } from '../shared/admin-api.service';
import { Author, Publisher } from '../shared/api.models';
import { CatalogApiService } from '../shared/catalog-api.service';
import { ConfirmDialog } from '../shared/confirm-dialog';

@Component({
  selector: 'app-references-admin-page',
  imports: [MatButtonModule, MatCardModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSnackBarModule, MatTableModule, MatTabsModule, ReactiveFormsModule],
  template: `
    <header class="page-heading"><div><p class="eyebrow">Catalogue</p><h1>Auteurs et éditeurs</h1><p>Gère les références reliées aux jeux.</p></div></header>
    <mat-tab-group animationDuration="0ms">
      <mat-tab label="Auteurs">
        <mat-card class="panel"><mat-card-header><mat-card-title>{{ authorId() ? 'Modifier un auteur' : 'Ajouter un auteur' }}</mat-card-title></mat-card-header><mat-card-content>
          <form class="form-row" (submit)="$event.preventDefault(); saveAuthor()"><mat-form-field appearance="outline"><mat-label>Nom de l’auteur</mat-label><input matInput [formControl]="authorName" required /></mat-form-field><button mat-flat-button color="primary" [disabled]="authorName.invalid">{{ authorId() ? 'Enregistrer' : 'Ajouter' }}</button>@if (authorId()) { <button mat-button type="button" (click)="resetAuthor()">Annuler</button> }</form>
          <div class="table-scroll"><table mat-table [dataSource]="authors()" aria-label="Auteurs">
            <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Nom</th><td mat-cell *matCellDef="let item">{{ item.name }}</td></ng-container>
            <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let item"><button mat-button type="button" [attr.aria-label]="'Modifier l’auteur ' + item.name" (click)="editAuthor(item.id)">Modifier</button><button mat-button color="warn" type="button" [attr.aria-label]="'Supprimer l’auteur ' + item.name" (click)="removeAuthor(item)">Supprimer</button></td></ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr><tr mat-row *matRowDef="let row; columns: columns"></tr>
          </table></div>
        </mat-card-content></mat-card>
      </mat-tab>
      <mat-tab label="Éditeurs">
        <mat-card class="panel"><mat-card-header><mat-card-title>{{ publisherId() ? 'Modifier un éditeur' : 'Ajouter un éditeur' }}</mat-card-title></mat-card-header><mat-card-content>
          <form class="form-row" (submit)="$event.preventDefault(); savePublisher()"><mat-form-field appearance="outline"><mat-label>Nom de l’éditeur</mat-label><input matInput [formControl]="publisherName" required /></mat-form-field><button mat-flat-button color="primary" [disabled]="publisherName.invalid">{{ publisherId() ? 'Enregistrer' : 'Ajouter' }}</button>@if (publisherId()) { <button mat-button type="button" (click)="resetPublisher()">Annuler</button> }</form>
          <div class="table-scroll"><table mat-table [dataSource]="publishers()" aria-label="Éditeurs">
            <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Nom</th><td mat-cell *matCellDef="let item">{{ item.name }}</td></ng-container>
            <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let item"><button mat-button type="button" [attr.aria-label]="'Modifier l’éditeur ' + item.name" (click)="editPublisher(item.id)">Modifier</button><button mat-button color="warn" type="button" [attr.aria-label]="'Supprimer l’éditeur ' + item.name" (click)="removePublisher(item)">Supprimer</button></td></ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr><tr mat-row *matRowDef="let row; columns: columns"></tr>
          </table></div>
        </mat-card-content></mat-card>
      </mat-tab>
    </mat-tab-group>
  `,
  styles: [`:host{display:block}.page-heading{margin:0 0 1.5rem}.page-heading h1{margin:0;font-size:clamp(1.8rem,4vw,2.5rem)}.page-heading p{color:#586579}.eyebrow{color:#3949ab;font-size:.75rem;font-weight:700;letter-spacing:.1em;text-transform:uppercase}.panel{margin:1rem 0;border-radius:16px}.form-row{display:flex;align-items:center;flex-wrap:wrap;gap:.5rem;padding-top:1rem}.form-row mat-form-field{width:min(100%,360px)}.table-scroll{overflow:auto}table{width:100%;min-width:420px}`],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReferencesAdminPage implements OnInit {
  private readonly admin = inject(AdminApiService);
  private readonly catalog = inject(CatalogApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  readonly columns = ['name', 'actions'];
  readonly authors = signal<Author[]>([]);
  readonly publishers = signal<Publisher[]>([]);
  readonly authorId = signal<number | null>(null);
  readonly publisherId = signal<number | null>(null);
  readonly authorName = new FormControl('', { nonNullable: true, validators: [Validators.required] });
  readonly publisherName = new FormControl('', { nonNullable: true, validators: [Validators.required] });

  ngOnInit(): void { this.loadAuthors(); this.loadPublishers(); }
  private loadAuthors(): void { this.catalog.getAuthors().subscribe({ next: (list) => this.authors.set(list), error: () => this.snack.open('Impossible de charger les auteurs.', 'Fermer') }); }
  private loadPublishers(): void { this.catalog.getPublishers().subscribe({ next: (list) => this.publishers.set(list), error: () => this.snack.open('Impossible de charger les éditeurs.', 'Fermer') }); }
  editAuthor(id: number): void { this.catalog.getAuthor(id).subscribe({ next: (item) => { this.authorId.set(id); this.authorName.setValue(item.name); }, error: () => this.snack.open('Auteur introuvable.', 'Fermer') }); }
  editPublisher(id: number): void { this.catalog.getPublisher(id).subscribe({ next: (item) => { this.publisherId.set(id); this.publisherName.setValue(item.name); }, error: () => this.snack.open('Éditeur introuvable.', 'Fermer') }); }
  saveAuthor(): void { const name = this.authorName.value.trim(); if (!name) return; const id = this.authorId(); const request$ = id ? this.admin.updateAuthor(id, name) : this.admin.createAuthor(name); request$.subscribe({ next: () => { this.snack.open(id ? 'Auteur modifié.' : 'Auteur ajouté.', 'Fermer', { duration: 2200 }); this.resetAuthor(); this.loadAuthors(); }, error: () => this.snack.open('Enregistrement de l’auteur impossible.', 'Fermer') }); }
  savePublisher(): void { const name = this.publisherName.value.trim(); if (!name) return; const id = this.publisherId(); const request$ = id ? this.admin.updatePublisher(id, name) : this.admin.createPublisher(name); request$.subscribe({ next: () => { this.snack.open(id ? 'Éditeur modifié.' : 'Éditeur ajouté.', 'Fermer', { duration: 2200 }); this.resetPublisher(); this.loadPublishers(); }, error: () => this.snack.open('Enregistrement de l’éditeur impossible.', 'Fermer') }); }
  resetAuthor(): void { this.authorId.set(null); this.authorName.reset(''); }
  resetPublisher(): void { this.publisherId.set(null); this.publisherName.reset(''); }
  removeAuthor(item: Author): void { this.dialog.open(ConfirmDialog, { data: { title: 'Supprimer cet auteur ?', message: `« ${item.name} » sera supprimé s’il n’est lié à aucun jeu.` } }).afterClosed().subscribe((confirmed: boolean) => { if (!confirmed) return; this.admin.deleteAuthor(item.id).subscribe({ next: () => { this.snack.open('Auteur supprimé.', 'Fermer', { duration: 2200 }); this.loadAuthors(); }, error: () => this.snack.open('Suppression impossible : cet auteur est peut-être lié à un jeu.', 'Fermer') }); }); }
  removePublisher(item: Publisher): void { this.dialog.open(ConfirmDialog, { data: { title: 'Supprimer cet éditeur ?', message: `« ${item.name} » sera supprimé s’il n’est lié à aucun jeu.` } }).afterClosed().subscribe((confirmed: boolean) => { if (!confirmed) return; this.admin.deletePublisher(item.id).subscribe({ next: () => { this.snack.open('Éditeur supprimé.', 'Fermer', { duration: 2200 }); this.loadPublishers(); }, error: () => this.snack.open('Suppression impossible : cet éditeur est peut-être lié à un jeu.', 'Fermer') }); }); }
}
