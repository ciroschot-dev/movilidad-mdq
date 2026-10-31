import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

// Pantalla para crear una cuenta: si sale bien, el usuario queda logueado y va a cotizar
@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
})
export class Registro {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  // Aviso que se muestra si el back no pudo crear la cuenta
  protected readonly error = signal('');
  // true mientras espera al back: bloquea el botón para no mandar la cuenta dos veces
  protected readonly cargando = signal(false);

  // Mismas reglas que RegistroRequest del backend, para avisar antes de mandar el pedido.
  // Cada campo: [valor inicial, validaciones].
  protected readonly registroForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  // Error solo si el campo está mal Y el usuario ya lo tocó; si no, se vería en rojo apenas abre la página
  protected mostrarError(control: string) {
    return this.registroForm.get(control)?.invalid && this.registroForm.get(control)?.touched;
  }

  protected registrar() {
    this.error.set('');
    this.cargando.set(true);
    // getRawValue() da el objeto con todos los campos, con la forma de RegistroForm
    this.auth.registrar(this.registroForm.getRawValue()).subscribe({
      // Salió bien: el back ya devuelve el token, así que queda logueado sin pasar por el login
      next: (sesion) => {
        this.auth.guardarSesion(sesion);
        this.router.navigate(['/cotizar']);
      },
      // Salió mal (por ejemplo, el usuario ya existe): muestra el aviso y libera el botón
      error: () => {
        this.error.set('No se pudo crear el usuario.');
        this.cargando.set(false);
      },
    });
  }
}
