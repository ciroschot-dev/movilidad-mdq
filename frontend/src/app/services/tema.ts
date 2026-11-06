import { Service, signal } from '@angular/core';

@Service()
export class Tema {
  // Arranca con la elección guardada para que el tema no se pierda al recargar
  readonly oscuro = signal(localStorage.getItem('tema') === 'oscuro');

  constructor() {
    this.aplicar();
  }

  alternar() {
    this.oscuro.update((valor) => !valor);
    localStorage.setItem('tema', this.oscuro() ? 'oscuro' : 'claro');
    this.aplicar();
  }

  // La clase dark en el <html> activa los colores de html.dark en styles.css
  private aplicar() {
    document.documentElement.classList.toggle('dark', this.oscuro());
  }
}
