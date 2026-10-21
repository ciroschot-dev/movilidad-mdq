import { Component, inject, signal } from '@angular/core';
import {
  email,
  form,
  FormField,
  FormRoot,
  maxLength,
  minLength,
  required,
} from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { RegistroForm } from '../../models/registro';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-registro',
  imports: [FormRoot, FormField, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
})
export class Registro {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly error = signal('');
  protected readonly datos = signal<RegistroForm>({ username: '', email: '', password: '' });

  // Mismas reglas que RegistroRequest del backend, para avisar antes de mandar el pedido
  protected readonly registroForm = form(
    this.datos,
    (campo) => {
      required(campo.username, { message: 'El usuario es obligatorio.' });
      minLength(campo.username, 3, { message: 'El usuario debe tener entre 3 y 30 caracteres.' });
      maxLength(campo.username, 30, { message: 'El usuario debe tener entre 3 y 30 caracteres.' });
      required(campo.email, { message: 'El email es obligatorio.' });
      email(campo.email, { message: 'El email no tiene un formato válido.' });
      required(campo.password, { message: 'La contraseña es obligatoria.' });
      minLength(campo.password, 6, { message: 'La contraseña debe tener al menos 6 caracteres.' });
    },
    {
      // Solo se ejecuta si el formulario es válido; si no, marca los campos y muestra los errores
      submission: { action: async () => this.registrar() },
    },
  );

  private registrar() {
    this.error.set('');
    this.auth.registrar(this.datos()).subscribe({
      // El backend ya devuelve el token, así que el usuario queda logueado sin pasar por el login
      next: (sesion) => {
        this.auth.guardarSesion(sesion);
        this.router.navigate(['/cotizar']);
      },
      error: () => this.error.set('No se pudo crear el usuario.'),
    });
  }
}
