import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';
import { AuthApiService } from './auth-api.service';

@Component({
  selector: 'app-register-page',
  imports: [MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, RouterLink],
  template: `
    <main class="auth-layout">
      <mat-card class="auth-card" aria-labelledby="register-title"><mat-card-content>
        <a class="brand" routerLink="/login">GamesUP</a>
        <p class="eyebrow">Rejoins la communauté</p>
        <h1 id="register-title">Créer un compte</h1>
        <p class="intro">Quelques informations suffisent pour commencer.</p>

        <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <mat-form-field appearance="outline"><mat-label>Nom</mat-label><input matInput
            id="name"
            type="text"
            autocomplete="name"
            formControlName="name"
            [attr.aria-invalid]="form.controls.name.touched && form.controls.name.invalid"
            [attr.aria-describedby]="form.controls.name.touched && form.controls.name.invalid ? 'name-error' : null"
            required
          /></mat-form-field>
          @if (form.controls.name.touched && form.controls.name.invalid) {
            <p class="field-error" id="name-error">Le nom est obligatoire.</p>
          }

          <mat-form-field appearance="outline"><mat-label>Adresse e-mail</mat-label><input matInput
            id="email"
            type="email"
            autocomplete="email"
            formControlName="email"
            [attr.aria-invalid]="form.controls.email.touched && form.controls.email.invalid"
            [attr.aria-describedby]="form.controls.email.touched && form.controls.email.invalid ? 'email-error' : null"
            required
          /></mat-form-field>
          @if (form.controls.email.touched && form.controls.email.invalid) {
            <p class="field-error" id="email-error">Saisis une adresse e-mail valide.</p>
          }

          <mat-form-field appearance="outline"><mat-label>Mot de passe</mat-label><input matInput
            id="password"
            type="password"
            autocomplete="new-password"
            formControlName="password"
            [attr.aria-invalid]="form.controls.password.touched && form.controls.password.invalid"
            [attr.aria-describedby]="form.controls.password.touched && form.controls.password.invalid ? 'password-error' : null"
            required
          /></mat-form-field>
          @if (form.controls.password.touched && form.controls.password.invalid) {
            <p class="field-error" id="password-error">Le mot de passe doit contenir au moins 6 caractères.</p>
          }

          @if (errorMessage()) {
            <p class="form-message error-message" role="alert">{{ errorMessage() }}</p>
          }
          @if (successMessage()) {
            <p class="form-message success-message" role="status">{{ successMessage() }}</p>
          }

          <button mat-flat-button color="primary" type="submit" [disabled]="loading()">
            {{ loading() ? 'Création…' : 'Créer mon compte' }}
          </button>
        </form>

        <p class="form-footer">Déjà inscrit ? <a routerLink="/login">Se connecter</a></p>
      </mat-card-content></mat-card>
    </main>
  `,
  styleUrl: './auth-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPage {
  private readonly auth = inject(AuthApiService);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(6)] }),
  });

  submit(): void {
    this.form.markAllAsTouched();
    this.errorMessage.set('');
    this.successMessage.set('');
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    this.auth.register(this.form.getRawValue()).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: () => this.successMessage.set('Compte créé. Tu peux maintenant te connecter.'),
      error: (error: unknown) => {
        const status = typeof error === 'object' && error !== null && 'status' in error
          ? error.status
          : undefined;
        this.errorMessage.set(status === 409
          ? 'Cette adresse e-mail est déjà utilisée.'
          : status === 0
            ? 'Impossible de joindre l’API. Vérifie qu’elle est démarrée.'
            : 'La création du compte a échoué. Vérifie les informations saisies.');
      },
    });
  }
}
