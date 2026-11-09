import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DireccionFavorita } from '../../models/lugar';
import { AvisoService } from '../../services/avisos';
import { ViajeService } from '../../services/viaje';

// Pantalla Favoritos: las direcciones que se guardaron al marcar viajes con la ⭐ (RF18),
// con los botones para ponerles un nombre o quitarlas (RF27)
@Component({
  selector: 'app-favoritos',
  imports: [ReactiveFormsModule],
  templateUrl: './favoritos.html',
  styleUrl: './favoritos.css',
})
export class Favoritos {
  private readonly viajes = inject(ViajeService);
  private readonly avisos = inject(AvisoService);
  private readonly fb = inject(FormBuilder);

  // El id de la dirección que se está renombrando; null cuando no se edita ninguna
  protected readonly editando = signal<number | null>(null);
  // El id de la dirección que espera la respuesta del back: bloquea sus botones para evitar el doble clic
  protected readonly ocupada = signal<number | null>(null);

  // Se edita de a una dirección, así que alcanza con un solo formulario. El máximo es el de la columna del back
  protected readonly nombreForm = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
  });

  // Las direcciones que devolvió el back
  protected readonly direcciones = signal<DireccionFavorita[]>([]);
  // true hasta que responde el back: mientras, no se muestra "No tenés direcciones favoritas" por error
  protected readonly cargando = signal(true);
  // Aviso que se muestra si el back no pudo traer las direcciones
  protected readonly error = signal('');

  constructor() {
    this.viajes.direccionesFavoritas().subscribe({
      next: (direcciones) => {
        this.direcciones.set(direcciones);
        this.cargando.set(false);
      },
      // Si el back explica por qué falló, se muestra su texto; si no respondió, uno genérico
      error: (respuesta: HttpErrorResponse) => {
        this.error.set(respuesta.error?.message ?? 'No se pudieron cargar tus favoritos.');
        this.cargando.set(false);
      },
    });
  }

  // El error se muestra recién cuando se tocó el campo, igual que en los otros formularios
  protected mostrarError(control: string) {
    return this.nombreForm.get(control)?.invalid && this.nombreForm.get(control)?.touched;
  }

  // Botón Renombrar: abre el campo con el nombre que ya tiene, para corregirlo sin escribirlo de cero
  protected empezarARenombrar(direccion: DireccionFavorita) {
    this.nombreForm.reset({ nombre: direccion.nombre ?? '' });
    this.editando.set(direccion.id);
  }

  protected cancelar() {
    this.editando.set(null);
  }

  // Guarda el nombre nuevo. La tarjeta cambia recién cuando el back confirma, para no mostrar algo que no se guardó
  protected guardar(direccion: DireccionFavorita) {
    const nombre = this.nombreForm.getRawValue().nombre;
    this.ocupada.set(direccion.id);
    this.viajes.renombrarDireccion(direccion.id, nombre).subscribe({
      next: () => {
        this.direcciones.update((direcciones) =>
          direcciones.map((d) => (d.id === direccion.id ? { ...d, nombre } : d)),
        );
        this.ocupada.set(null);
        this.editando.set(null);
        this.avisos.mostrar('Nombre guardado.');
      },
      error: (respuesta: HttpErrorResponse) => {
        this.ocupada.set(null);
        this.avisos.mostrar(respuesta.error?.message ?? 'No se pudo guardar el nombre.', 'error');
      },
    });
  }

  // Botón Quitar: primero pregunta, porque una dirección quitada no se puede recuperar desde acá
  protected confirmarQuitar(direccion: DireccionFavorita) {
    this.avisos.confirmar('¿Quitar esta dirección de Favoritos?', () => this.quitar(direccion));
  }

  private quitar(direccion: DireccionFavorita) {
    this.ocupada.set(direccion.id);
    this.viajes.quitarDireccion(direccion.id).subscribe({
      next: () => {
        this.direcciones.update((direcciones) => direcciones.filter((d) => d.id !== direccion.id));
        this.ocupada.set(null);
        this.avisos.mostrar('Dirección quitada.');
      },
      error: (respuesta: HttpErrorResponse) => {
        this.ocupada.set(null);
        this.avisos.mostrar(respuesta.error?.message ?? 'No se pudo quitar la dirección.', 'error');
      },
    });
  }
}
