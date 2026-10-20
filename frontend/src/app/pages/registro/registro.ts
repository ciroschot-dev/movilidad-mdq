import { Component, signal } from '@angular/core';
import {
  email,
  form,
  FormField,
  FormRoot,
  maxLength,
  minLength,
  required,
} from '@angular/forms/signals';
import { RegistroForm } from '../../models/registro';

@Component({
  selector: 'app-registro',
  imports: [FormRoot, FormField],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
})
export class Registro {
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
      submission: { action: async () => {} },
    },
  );
}
