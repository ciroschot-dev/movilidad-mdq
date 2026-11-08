import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { switchMap } from 'rxjs';
import { ViajeHistorial } from '../../models/viaje';
import { UsuarioService } from '../../services/usuario';

// Pantalla Historial: los viajes que cotizó el usuario, del más reciente al más viejo (RF13)
@Component({
  selector: 'app-historial',
  imports: [],
  templateUrl: './historial.html',
  styleUrl: './historial.css',
})
export class Historial {
  private readonly usuarios = inject(UsuarioService);

  // Los viajes que devolvió el back
  protected readonly viajes = signal<ViajeHistorial[]>([]);
  // true hasta que responde el back: mientras, no se muestra "Todavía no tenés viajes" por error
  protected readonly cargando = signal(true);
  // Aviso que se muestra si el back no pudo traer el historial
  protected readonly error = signal('');

  constructor() {
    // El back pide el id de la cuenta en la dirección, y la sesión guarda solo el token:
    // primero se trae la cuenta (GET /usuarios/me) y con su id se pide el historial
    this.usuarios
      .actual()
      .pipe(switchMap((usuario) => this.usuarios.historial(usuario.id)))
      .subscribe({
        next: (viajes) => {
          this.viajes.set(viajes);
          this.cargando.set(false);
        },
        // Si el back explica por qué falló, se muestra su texto; si no respondió, uno genérico
        error: (respuesta: HttpErrorResponse) => {
          this.error.set(respuesta.error?.message ?? 'No se pudo cargar tu historial.');
          this.cargando.set(false);
        },
      });
  }

  // El back manda la fecha sin zona (2026-06-03T14:30:00): se muestra como en Argentina (3/6/26, 14:30)
  protected formatearFecha(fechaHora: string) {
    return new Date(fechaHora).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' });
  }

  // Los metros pasan a km con un decimal y coma, como en Argentina (8,2 km)
  protected formatearKm(metros: number) {
    return (metros / 1000).toLocaleString('es-AR', { maximumFractionDigits: 1 }) + ' km';
  }

  // Igual que en Cotizar ($ 4.533). Si esa app no se pudo cotizar, el back manda null: se ve una raya
  protected formatearPrecio(precio: number | null) {
    return precio === null ? '—' : '$ ' + Math.round(precio).toLocaleString('es-AR');
  }
}
