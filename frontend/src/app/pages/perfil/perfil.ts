import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth';
import { AvisoService } from '../../services/avisos';
import { UsuarioService } from '../../services/usuario';

// Pantalla "Mi perfil": ver los datos de la cuenta (RF04) y modificarlos (RF05)
@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil {
  private readonly usuarios = inject(UsuarioService);
  private readonly auth = inject(AuthService);
  private readonly avisos = inject(AvisoService);
  private readonly fb = inject(FormBuilder);

  // Aviso que se muestra si el back no pudo traer el perfil: en ese caso no hay formulario
  protected readonly errorCarga = signal('');
  // Aviso que se muestra arriba del botón si el back no pudo guardar el perfil
  protected readonly error = signal('');
  // true hasta que llegan los datos: mientras, el formulario no se muestra
  protected readonly cargando = signal(true);
  // true mientras guarda: bloquea el botón para no mandar el perfil dos veces
  protected readonly guardando = signal(false);

  // El id de la cuenta, que el back pide en la dirección para guardar
  private id = 0;
  // El nombre de usuario con el que se inició sesión, para saber si se cambió al guardar
  private usuarioOriginal = '';

  // Mismas reglas que el registro, así un dato mal cargado se avisa antes de mandarlo.
  // La contraseña no es obligatoria: vacía = no se cambia (minLength no se queja de un campo vacío)
  protected readonly perfilForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.minLength(6)],
  });

  constructor() {
    // Al abrir la pantalla le pide al back los datos de la cuenta y los carga en el formulario
    this.usuarios.actual().subscribe({
      // patchValue y no setValue: el back manda también el id y el rol, que el formulario no tiene
      next: (usuario) => {
        this.id = usuario.id;
        this.usuarioOriginal = usuario.username;
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

  // Guarda los datos nuevos (RF05)
  protected guardar() {
    // El botón ya está bloqueado con datos inválidos; esto es por si se aprieta Enter igual
    if (this.perfilForm.invalid) {
      return;
    }
    this.error.set('');
    this.guardando.set(true);
    const datos = this.perfilForm.getRawValue();
    this.usuarios.actualizar(this.id, datos).subscribe({
      next: (usuario) => {
        // El token del back lleva el nombre de usuario: si se cambió, el token viejo ya no sirve
        // y el próximo pedido daría "Tu sesión venció". Por eso se cierra la sesión acá, con un aviso claro
        if (usuario.username !== this.usuarioOriginal) {
          this.avisos.mostrar('Perfil actualizado. Iniciá sesión con tu nuevo usuario.');
          this.auth.cerrarSesion();
          return;
        }
        // Si solo cambió el email o la contraseña, sigue adentro: se vacía la contraseña y se confirma
        this.perfilForm.patchValue({ ...usuario, password: '' });
        this.guardando.set(false);
        this.avisos.mostrar('Perfil actualizado.');
      },
      // Salió mal: muestra el motivo del back (ej.: "Ese email ya está registrado") o el aviso de la historia
      error: (respuesta: HttpErrorResponse) => {
        this.error.set(respuesta.error?.message ?? 'No se pudo actualizar el perfil.');
        this.guardando.set(false);
      },
    });
  }
}
