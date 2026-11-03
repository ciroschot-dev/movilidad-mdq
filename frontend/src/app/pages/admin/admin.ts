import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Usuario } from '../../models/usuario';
import { AvisoService } from '../../services/avisos';
import { UsuarioService } from '../../services/usuario';

// Pantalla del admin: busca una cuenta por nombre o email y la puede eliminar
@Component({
  selector: 'app-admin',
  imports: [ReactiveFormsModule],
  templateUrl: './admin.html',
  styleUrl: './admin.css',
})
export class Admin {
  private readonly usuarios = inject(UsuarioService);
  private readonly avisos = inject(AvisoService);
  private readonly fb = inject(FormBuilder);

  // El usuario encontrado; null = todavía no se buscó o no apareció
  protected readonly usuario = signal<Usuario | null>(null);
  // Aviso del back cuando la búsqueda falla (ej.: "Usuario no encontrado")
  protected readonly error = signal('');
  // true mientras espera al back: bloquea el botón para no buscar dos veces
  protected readonly cargando = signal(false);

  // Un solo campo: el nombre de usuario o el email a buscar
  protected readonly busquedaForm = this.fb.nonNullable.group({
    query: ['', Validators.required],
  });

  // Error solo si el campo está mal Y el usuario ya lo tocó
  protected mostrarError(control: string) {
    return this.busquedaForm.get(control)?.invalid && this.busquedaForm.get(control)?.touched;
  }

  protected buscar() {
    this.error.set('');
    this.usuario.set(null);
    this.cargando.set(true);
    // trim() saca los espacios de más, así " juan " encuentra a "juan"
    this.usuarios.buscar(this.busquedaForm.getRawValue().query.trim()).subscribe({
      next: (encontrado) => {
        // Las cuentas ADMIN no se muestran, así nadie ve el email del admin ni la puede borrar
        if (encontrado.role === 'ADMIN') {
          this.error.set('Usuario no encontrado');
        } else {
          this.usuario.set(encontrado);
        }
        this.cargando.set(false);
      },
      // Si el back explica por qué falló, mostramos su texto; si no respondió, uno genérico
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? 'No se pudo buscar el usuario. Probá de nuevo en un rato.');
        this.cargando.set(false);
      },
    });
  }

  // Pregunta antes de borrar, porque eliminar una cuenta no tiene vuelta atrás
  protected confirmarEliminar(usuario: Usuario) {
    this.avisos.confirmar(`¿Eliminar la cuenta de ${usuario.username}?`, () => this.eliminar(usuario.id));
  }

  private eliminar(id: number) {
    this.usuarios.eliminar(id).subscribe({
      // Salió bien: saca la tarjeta y avisa
      next: () => {
        this.usuario.set(null);
        this.avisos.mostrar('Usuario eliminado correctamente.');
      },
      error: (err: HttpErrorResponse) =>
        this.avisos.mostrar(err.error?.message ?? 'No se pudo eliminar el usuario.', 'error'),
    });
  }
}
