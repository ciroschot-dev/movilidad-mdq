import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { form, FormField, FormRoot, required } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { Credenciales } from '../../models/auth';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormRoot, FormField, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly error = signal('');
  protected readonly datos = signal<Credenciales>({ username: '', password: '' });

  // Mismas reglas que LoginRequest del backend: los dos campos son obligatorios
  protected readonly loginForm = form(
    this.datos,
    (campo) => {
      required(campo.username, { message: 'El usuario es obligatorio.' });
      required(campo.password, { message: 'La contraseña es obligatoria.' });
    },
    {
      // Solo se ejecuta si el formulario es válido; si no, marca los campos y muestra los errores
      submission: { action: async () => this.entrar() },
    },
  );

  private entrar() {
    this.error.set('');
    this.auth.login(this.datos()).subscribe({
      next: (sesion) => {
        this.auth.guardarSesion(sesion);
        this.router.navigate(['/cotizar']);
      },
      // 401 = usuario o contraseña mal; cualquier otro error es que el back no respondió bien
      error: (respuesta: HttpErrorResponse) =>
        this.error.set(
          respuesta.status === 401
            ? 'Usuario o contraseña incorrectos.'
            : 'No se pudo iniciar sesión. Probá de nuevo en un rato.',
        ),
    });
  }
}
