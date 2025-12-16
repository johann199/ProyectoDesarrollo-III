import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { FieldErrorComponent } from '../../../shared/field-error/field-error.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    ReactiveFormsModule, 
    MatIconModule,
    FieldErrorComponent
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  message = '';
  showPassword = false;
  logoLoaded = false;

  constructor(
    private readonly authService: AuthService, 
    private readonly fb: FormBuilder,
    private readonly router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  ngOnInit() {
    // Verificar si el usuario ya está autenticado usando el método corregido
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onLogoLoad() {
    this.logoLoaded = true;
  }

  onLogoError() {
    this.logoLoaded = false;
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  login() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.message = '❌ Por favor, completa todos los campos correctamente';
      return;
    }

    const { username, password } = this.loginForm.value;
    this.loading = true;
    this.message = '';

    this.authService.login(username, password).subscribe({
      next: (user) => {
        this.message = `✅ Bienvenido/a, ${user.name}`;
        this.loading = false;
        // La redirección se maneja en el AuthService
      },
      error: (err) => {
        this.loading = false;
        this.message = '❌ ' + (err.error?.message || 'Error en la autenticación. Verifica tus credenciales.');
        console.error('Login error:', err);
      },
    });
  }
}