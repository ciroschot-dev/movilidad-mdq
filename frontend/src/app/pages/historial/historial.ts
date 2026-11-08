import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { switchMap, tap } from 'rxjs';
import { ViajeFrecuente, ViajeHistorial } from '../../models/viaje';
import { AvisoService } from '../../services/avisos';
import { UsuarioService } from '../../services/usuario';

// Pantalla Historial: los viajes que cotizó el usuario, del más reciente al más viejo (RF13), borrarlos (RF14)
// y el viaje que más repite (RF15)
@Component({
  selector: 'app-historial',
  imports: [],
  templateUrl: './historial.html',
  styleUrl: './historial.css',
})
export class Historial {
  private readonly usuarios = inject(UsuarioService);
  private readonly avisos = inject(AvisoService);

  // Los viajes que devolvió el back
  protected readonly viajes = signal<ViajeHistorial[]>([]);
  // true hasta que responde el back: mientras, no se muestra "Todavía no tenés viajes" por error
  protected readonly cargando = signal(true);
  // Aviso que se muestra si el back no pudo traer el historial
  protected readonly error = signal('');
  // El id del viaje que se está borrando, para bloquear su botón y no mandarlo dos veces
  protected readonly borrando = signal<number | null>(null);
  // El viaje que más repite; null = no tiene ninguno, y la tarjeta no aparece
  protected readonly frecuente = signal<ViajeFrecuente | null>(null);

  // El id de la cuenta, que el back pide en la dirección para traer y para borrar viajes
  private id = 0;

  constructor() {
    // El back pide el id de la cuenta en la dirección, y la sesión guarda solo el token:
    // primero se trae la cuenta (GET /usuarios/me) y con su id se pide el historial
    this.usuarios
      .actual()
      .pipe(
        tap((usuario) => {
          this.id = usuario.id;
          this.cargarFrecuente();
        }),
        switchMap((usuario) => this.usuarios.historial(usuario.id)),
      )
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

  // Borra el viaje (RF14). Recién cuando el back confirma se saca de la lista, así no desaparece si falló
  protected borrar(viaje: ViajeHistorial) {
    this.borrando.set(viaje.id);
    this.usuarios.borrarViaje(this.id, viaje.id).subscribe({
      next: () => {
        this.viajes.update((viajes) => viajes.filter((v) => v.id !== viaje.id));
        this.borrando.set(null);
        this.avisos.mostrar('Viaje borrado.');
        // Con un viaje menos el frecuente puede cambiar o dejar de existir: se le vuelve a pedir al back
        this.cargarFrecuente();
      },
      error: (respuesta: HttpErrorResponse) => {
        this.borrando.set(null);
        this.avisos.mostrar(respuesta.error?.message ?? 'No se pudo borrar el viaje.', 'error');
      },
    });
  }

  // Pide el viaje frecuente (RF15). Si falla no se avisa: es un extra y el historial se ve igual
  private cargarFrecuente() {
    this.usuarios.viajeFrecuente(this.id).subscribe({
      next: (frecuente) => this.frecuente.set(frecuente),
      error: () => this.frecuente.set(null),
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
