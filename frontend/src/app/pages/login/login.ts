import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

// Pantalla para entrar con usuario y contraseña: si sale bien, va a cotizar
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  // Aviso que se muestra si el back rechazó el login
  protected readonly error = signal('');
  // true mientras espera al back: bloquea el botón para no mandar el login dos veces
  protected readonly cargando = signal(false);

  // Mismas reglas que LoginRequest del backend: los dos campos son obligatorios.
  // Cada campo: [valor inicial, validaciones].
  protected readonly loginForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  // Error solo si el campo está mal Y el usuario ya lo tocó; si no, se vería en rojo apenas abre la página
  protected mostrarError(control: string) {
    return this.loginForm.get(control)?.invalid && this.loginForm.get(control)?.touched;
  }

  protected entrar() {
    this.error.set('');
    this.cargando.set(true);
    // getRawValue() da el objeto con todos los campos, con la forma de Credenciales
    this.auth.login(this.loginForm.getRawValue()).subscribe({
      // Salió bien: guarda el token y entra
      next: (sesion) => {
        this.auth.guardarSesion(sesion);
        this.router.navigate(['/cotizar']);
      },
      // 401 = usuario o contraseña mal; cualquier otro error es que el back no respondió bien
      error: (respuesta: HttpErrorResponse) => {
        this.error.set(
          respuesta.status === 401
            ? 'Usuario o contraseña incorrectos.'
            : 'No se pudo iniciar sesión. Probá de nuevo en un rato.',
        );
        this.cargando.set(false);
      },
    });
  }
}
