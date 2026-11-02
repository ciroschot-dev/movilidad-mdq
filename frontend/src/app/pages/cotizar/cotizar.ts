import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Mapa } from '../../components/mapa/mapa';
import { OpcionTransporte } from '../../models/viaje';
import { ViajeService } from '../../services/viaje';

// Pantalla para cotizar: se escriben origen y destino, y el back devuelve cuánto sale en Taxi, Uber y Didi
@Component({
  selector: 'app-cotizar',
  imports: [ReactiveFormsModule, Mapa],
  templateUrl: './cotizar.html',
  styleUrl: './cotizar.css',
})
export class Cotizar {
  private readonly viajes = inject(ViajeService);
  private readonly fb = inject(FormBuilder);

  // Lo que devolvió el back; las tarjetas que las muestran llegan con RF09 (MDQ-24)
  protected readonly opciones = signal<OpcionTransporte[]>([]);
  // Aviso que se muestra si el back no pudo cotizar
  protected readonly error = signal('');
  // true mientras espera al back: bloquea el botón para no cotizar dos veces
  protected readonly cargando = signal(false);

  // Por ahora son texto común: el autocompletado de Google lo suma RF08 (MDQ-23)
  protected readonly busquedaForm = this.fb.nonNullable.group({
    origen: ['', Validators.required],
    destino: ['', Validators.required],
  });

  // Error solo si el campo está vacío Y el usuario ya lo tocó; si no, se vería en rojo apenas abre la página
  protected mostrarError(control: string) {
    return this.busquedaForm.get(control)?.invalid && this.busquedaForm.get(control)?.touched;
  }

  protected calcular() {
    this.error.set('');
    this.cargando.set(true);
    // getRawValue() da el objeto { origen, destino }, con la forma de BusquedaForm
    this.viajes.calcular(this.busquedaForm.getRawValue()).subscribe({
      // Salió bien: guarda las opciones y libera el botón
      next: (opciones) => {
        this.opciones.set(opciones);
        this.cargando.set(false);
      },
      // Salió mal: borra las opciones viejas para no mostrarlas junto al aviso
      error: () => {
        this.opciones.set([]);
        this.error.set('No se pudieron calcular las opciones.');
        this.cargando.set(false);
      },
    });
  }
}
