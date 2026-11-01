import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AdminApiService } from '../shared/admin-api.service';
import { User } from '../shared/api.models';
import { ConfirmDialog } from '../shared/confirm-dialog';

@Component({
  selector: 'app-users-admin-page',
  imports: [MatButtonModule, MatCardModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSnackBarModule, MatTableModule, ReactiveFormsModule],
  template: `
    <header class="page-heading"><div><p class="eyebrow">Administration</p><h1>Comptes utilisateurs</h1><p>Consulte et mets à jour les informations de compte. Les rôles sont affichés en lecture seule.</p></div></header>
    @if (selectedUser()) {
      <mat-card class="editor"><mat-card-header><mat-card-title>Modifier le compte de {{ selectedUser()?.name }}</mat-card-title></mat-card-header><mat-card-content>
        <form [formGroup]="form" (ngSubmit)="save()" class="form-row">
          <mat-form-field appearance="outline"><mat-label>Nom</mat-label><input matInput formControlName="name" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Adresse e-mail</mat-label><input matInput type="email" formControlName="email" required /></mat-form-field>
          <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">{{ saving() ? 'Enregistrement…' : 'Enregistrer' }}</button>
          <button mat-button type="button" (click)="cancelEdit()">Annuler</button>
        </form>
      </mat-card-content></mat-card>
    }
    <mat-card class="table-card"><mat-card-header><mat-card-title>Comptes ({{ users().length }})</mat-card-title></mat-card-header>
      @if (loading()) { <p class="state" role="status">Chargement des comptes…</p> }
      @else if (error()) { <p class="state error" role="alert">{{ error() }}</p> }
      @else if (users().length === 0) { <p class="state">Aucun compte utilisateur.</p> }
      @else { <div class="table-scroll"><table mat-table [dataSource]="users()" aria-label="Comptes utilisateurs">
        <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Nom</th><td mat-cell *matCellDef="let user">{{ user.name }}</td></ng-container>
        <ng-container matColumnDef="email"><th mat-header-cell *matHeaderCellDef>E-mail</th><td mat-cell *matCellDef="let user">{{ user.email }}</td></ng-container>
        <ng-container matColumnDef="role"><th mat-header-cell *matHeaderCellDef>Rôle</th><td mat-cell *matCellDef="let user"><span class="role-badge">{{ user.role === 'ROLE_ADMIN' ? 'Administrateur' : 'Client' }}</span></td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let user"><button mat-button type="button" [attr.aria-label]="'Consulter et modifier ' + user.name" (click)="edit(user.id)">Détails / modifier</button><button mat-button color="warn" type="button" [attr.aria-label]="'Supprimer le compte de ' + user.name" (click)="remove(user)">Supprimer</button></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="columns"></tr><tr mat-row *matRowDef="let row; columns: columns"></tr>
      </table></div> }
    </mat-card>
  `,
  styles: [`:host{display:block}.page-heading{margin:0 0 1.5rem}.page-heading h1{margin:0;font-size:clamp(1.8rem,4vw,2.5rem)}.page-heading p{color:#586579}.eyebrow{color:#3949ab;font-size:.75rem;font-weight:700;letter-spacing:.1em;text-transform:uppercase}.editor,.table-card{margin-bottom:1.5rem;border-radius:16px}.form-row{display:flex;align-items:center;flex-wrap:wrap;gap:.5rem;padding-top:1rem}.form-row mat-form-field{width:min(100%,320px)}.table-scroll{overflow:auto}table{min-width:650px;width:100%}.role-badge{padding:.25rem .65rem;border-radius:99px;background:#e8eaf6;color:#303f9f;font-weight:600}.state{padding:1.5rem;color:#56625d}.error{color:#b3261e}`],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersAdminPage implements OnInit {
  private readonly api = inject(AdminApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  readonly columns = ['name', 'email', 'role', 'actions'];
  readonly users = signal<User[]>([]);
  readonly selectedUser = signal<User | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
  });

  ngOnInit(): void { this.load(); }
  load(): void { this.loading.set(true); this.error.set(''); this.api.getUsers().subscribe({ next: (value) => { this.users.set(value); this.loading.set(false); }, error: () => { this.error.set('Impossible de charger les comptes.'); this.loading.set(false); } }); }
  edit(id: number): void { this.api.getUser(id).subscribe({ next: (user) => { this.selectedUser.set(user); this.form.setValue({ name: user.name, email: user.email }); }, error: () => this.snack.open('Ce compte est introuvable.', 'Fermer') }); }
  save(): void { this.form.markAllAsTouched(); if (this.form.invalid || this.saving()) return; const user = this.selectedUser(); if (!user) return; this.saving.set(true); this.api.updateUser(user.id, { name: this.form.controls.name.value.trim(), email: this.form.controls.email.value.trim() }).subscribe({ next: () => { this.snack.open('Compte modifié.', 'Fermer', { duration: 2300 }); this.cancelEdit(); this.load(); this.saving.set(false); }, error: () => { this.snack.open('Modification impossible. Vérifie que l’adresse e-mail est disponible.', 'Fermer'); this.saving.set(false); } }); }
  cancelEdit(): void { this.selectedUser.set(null); this.form.reset({ name: '', email: '' }); }
  remove(user: User): void { this.dialog.open(ConfirmDialog, { data: { title: 'Supprimer ce compte ?', message: `Le compte de ${user.name} sera supprimé s’il ne possède aucune commande.` } }).afterClosed().subscribe((confirmed: boolean) => { if (!confirmed) return; this.api.deleteUser(user.id).subscribe({ next: () => { this.snack.open('Compte supprimé.', 'Fermer', { duration: 2300 }); if (this.selectedUser()?.id === user.id) this.cancelEdit(); this.load(); }, error: () => this.snack.open('Suppression impossible : ce compte est peut-être lié à une commande.', 'Fermer') }); }); }
}
