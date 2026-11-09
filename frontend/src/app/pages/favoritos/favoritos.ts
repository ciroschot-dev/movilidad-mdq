import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { DireccionFavorita } from '../../models/lugar';
import { ViajeService } from '../../services/viaje';

// Pantalla Favoritos: las direcciones que se guardaron al marcar viajes con la ⭐ (RF18)
@Component({
  selector: 'app-favoritos',
  imports: [],
  templateUrl: './favoritos.html',
  styleUrl: './favoritos.css',
})
export class Favoritos {
  private readonly viajes = inject(ViajeService);

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
}
