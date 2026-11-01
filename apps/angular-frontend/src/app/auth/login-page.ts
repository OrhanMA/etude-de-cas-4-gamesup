import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';
import { AuthApiService } from './auth-api.service';

@Component({
  selector: 'app-login-page',
  imports: [MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, ReactiveFormsModule, RouterLink],
  template: `
    <main class="auth-layout">
      <mat-card class="auth-card" aria-labelledby="login-title"><mat-card-content>
        <a class="brand" routerLink="/login">GamesUP</a>
        <p class="eyebrow">Espace membre</p>
        <h1 id="login-title">Connexion</h1>
        <p class="intro">Connecte-toi pour accéder à ton espace GamesUP.</p>

        <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
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
            autocomplete="current-password"
            formControlName="password"
            [attr.aria-invalid]="form.controls.password.touched && form.controls.password.invalid"
            [attr.aria-describedby]="form.controls.password.touched && form.controls.password.invalid ? 'password-error' : null"
            required
          /></mat-form-field>
          @if (form.controls.password.touched && form.controls.password.invalid) {
            <p class="field-error" id="password-error">Le mot de passe est obligatoire.</p>
          }

          @if (errorMessage()) {
            <p class="form-message error-message" role="alert">{{ errorMessage() }}</p>
          }

          <button mat-flat-button color="primary" type="submit" [disabled]="loading()">
            {{ loading() ? 'Connexion…' : 'Se connecter' }}
          </button>
        </form>

        <p class="form-footer">Pas encore de compte ? <a routerLink="/register">Créer un compte</a></p>
      </mat-card-content></mat-card>
    </main>
  `,
  styleUrl: './auth-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  private readonly auth = inject(AuthApiService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  submit(): void {
    this.form.markAllAsTouched();
    this.errorMessage.set('');
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    this.auth.login(this.form.getRawValue()).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: () => void this.router.navigateByUrl(this.auth.isAdmin() ? '/admin' : '/catalog'),
      error: (error: unknown) => {
        const status = typeof error === 'object' && error !== null && 'status' in error
          ? error.status
          : undefined;
        this.errorMessage.set(status === 0
          ? 'Impossible de joindre l’API. Vérifie qu’elle est démarrée.'
          : 'Adresse e-mail ou mot de passe incorrect.');
      },
    });
  }
}
