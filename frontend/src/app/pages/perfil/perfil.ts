import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsuarioService } from '../../services/usuario';

// Pantalla "Mi perfil": muestra el usuario y el email de la cuenta con la que se inició sesión (RF04)
@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil {
  private readonly usuarios = inject(UsuarioService);
  private readonly fb = inject(FormBuilder);

  // Aviso que se muestra si el back no pudo traer el perfil: en ese caso no hay formulario
  protected readonly errorCarga = signal('');
  // true hasta que llegan los datos: mientras, el formulario no se muestra
  protected readonly cargando = signal(true);

  // Mismas reglas que el registro, así un dato mal cargado se avisa antes de mandarlo
  protected readonly perfilForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    email: ['', [Validators.required, Validators.email]],
  });

  constructor() {
    // Al abrir la pantalla le pide al back los datos de la cuenta y los carga en el formulario
    this.usuarios.actual().subscribe({
      // patchValue y no setValue: el back manda también el id y el rol, que el formulario no tiene
      next: (usuario) => {
        this.perfilForm.patchValue(usuario);
        this.cargando.set(false);
      },
      // Si el back explica por qué falló, se muestra su texto; si no respondió, uno genérico
      error: (respuesta: HttpErrorResponse) => {
        this.errorCarga.set(respuesta.error?.message ?? 'No se pudo cargar tu perfil.');
        this.cargando.set(false);
      },
    });
  }

  // Error solo si el campo está mal Y el usuario ya lo tocó; si no, se vería en rojo apenas abre la página
  protected mostrarError(control: string) {
    return this.perfilForm.get(control)?.invalid && this.perfilForm.get(control)?.touched;
  }
}
