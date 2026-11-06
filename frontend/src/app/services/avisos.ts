import { Service, signal } from '@angular/core';
import { Aviso, Pregunta } from '../models/aviso';

// Avisos y confirmaciones propios, para no usar alert() ni confirm() del navegador.
// Cualquier pantalla los pide desde acá, y el componente Avisos de app.html los dibuja.
@Service()
export class AvisoService {
  // Qué aviso se ve ahora; null = ninguno
  readonly aviso = signal<Aviso | null>(null);
  // Qué pregunta espera respuesta; null = ninguna
  readonly pregunta = signal<Pregunta | null>(null);

  // Guarda el reloj del aviso actual, para cortarlo si llega otro antes de los 3 segundos
  private reloj = 0;

  // Muestra un aviso y lo saca solo a los 3 segundos.
  // Ej.: this.avisos.mostrar('No se pudo eliminar la cuenta.', 'error')
  mostrar(texto: string, tipo: 'exito' | 'error' = 'exito') {
    this.aviso.set({ texto, tipo });
    clearTimeout(this.reloj);
    this.reloj = setTimeout(() => this.aviso.set(null), 3000);
  }

  // Abre la ventana de Aceptar/Cancelar. alAceptar es lo que se hace si acepta.
  // Ej.: this.avisos.confirmar('¿Eliminar la cuenta?', () => this.eliminar())
  confirmar(texto: string, alAceptar: () => void) {
    this.pregunta.set({ texto, alAceptar });
  }

  // Botón Aceptar: cierra la ventana y recién ahí hace lo que se pidió
  aceptar() {
    const pregunta = this.pregunta();
    this.pregunta.set(null);
    pregunta?.alAceptar();
  }

  // Botón Cancelar: cierra la ventana sin hacer nada
  cancelar() {
    this.pregunta.set(null);
  }
}
