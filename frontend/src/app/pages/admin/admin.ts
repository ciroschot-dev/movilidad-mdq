import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Usuario } from '../../models/usuario';
import { AvisoService } from '../../services/avisos';
import { TarifaService } from '../../services/tarifa';
import { UsuarioService } from '../../services/usuario';

// Pantalla del admin: buscar y eliminar cuentas, y ajustar la tarifa del taxi
@Component({
  selector: 'app-admin',
  imports: [ReactiveFormsModule],
  templateUrl: './admin.html',
  styleUrl: './admin.css',
})
export class Admin {
  private readonly usuarios = inject(UsuarioService);
  private readonly tarifas = inject(TarifaService);
  private readonly avisos = inject(AvisoService);
  private readonly fb = inject(FormBuilder);

  // El usuario encontrado; null = todavía no se buscó o no apareció
  protected readonly usuario = signal<Usuario | null>(null);
  // Aviso del back cuando la búsqueda falla (ej.: "Usuario no encontrado")
  protected readonly error = signal('');
  // Aviso cuando no se pudo traer la tarifa del taxi al abrir la pantalla
  protected readonly errorTarifa = signal('');
  // true mientras espera al back: bloquea el botón para no buscar dos veces
  protected readonly cargando = signal(false);
  // true mientras guarda la tarifa, para no mandarla dos veces
  protected readonly guardando = signal(false);

  // Un solo campo: el nombre de usuario o el email a buscar
  protected readonly busquedaForm = this.fb.nonNullable.group({
    query: ['', Validators.required],
  });

  // Los cinco valores de la tarifa del taxi (MDQ-37); arrancan en cero y se llenan con lo que manda el back
  protected readonly tarifaForm = this.fb.nonNullable.group({
    bajadaBanderaDia: [0, Validators.min(0)],
    bajadaBanderaNoche: [0, Validators.min(0)],
    valorFichaDia: [0, Validators.min(0)],
    valorFichaNoche: [0, Validators.min(0)],
    metrosPorFicha: [0, Validators.min(1)],
  });

  constructor() {
    // Al abrir la pantalla, trae la tarifa de hoy para que el admin la vea antes de cambiar algo
    this.tarifas.obtener().subscribe({
      // patchValue y no setValue: el back devuelve también el id, el tipo y la fecha, y setValue
      // se queja si sobra algún campo. Así solo se copian los cinco que tiene el formulario
      next: (tarifa) => this.tarifaForm.patchValue(tarifa),
      // Si el back explica por qué falló, mostramos su texto; si no respondió, uno genérico
      error: (err: HttpErrorResponse) =>
        this.errorTarifa.set(err.error?.message ?? 'No se pudo traer la tarifa del taxi.'),
    });
  }

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

  // Guarda los cinco valores de la tarifa del taxi (MDQ-37)
  protected guardarTarifa() {
    // El botón ya está bloqueado con valores inválidos; esto es por si se aprieta Enter igual
    if (this.tarifaForm.invalid) {
      return;
    }
    this.guardando.set(true);
    this.tarifas
      .actualizar(this.tarifaForm.getRawValue())
      .pipe(
        // Pase lo que pase, el botón se destraba: ni una respuesta rara ni un aviso que falle
        // pueden dejarlo en "GUARDANDO…" para siempre
        finalize(() => this.guardando.set(false)),
      )
      .subscribe({
        next: (tarifa) => {
          // Se vuelve a llenar con lo que guardó el back, así en pantalla quedan los valores reales
          this.tarifaForm.patchValue(tarifa);
          this.errorTarifa.set('');
          this.avisos.mostrar('Tarifa del taxi actualizada.');
        },
        // Sin permiso el back lo dice con su propio texto; si no respondió, queda el aviso de la historia
        error: (err: HttpErrorResponse) =>
          this.avisos.mostrar(
            err.error?.message ?? 'No se pudo actualizar la tarifa. Verifica tus permisos.',
            'error',
          ),
      });
  }
}
