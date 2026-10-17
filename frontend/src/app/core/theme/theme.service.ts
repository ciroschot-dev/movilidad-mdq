import { DOCUMENT, Injectable, effect, inject, signal } from '@angular/core';

export type Tema = 'claro' | 'oscuro';

// Misma clave que usaba el front de React, así se respeta la preferencia guardada.
export const CLAVE_TEMA = 'movilidadmdq.theme.v1';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);

  readonly tema = signal<Tema>(this.leerTemaInicial());

  constructor() {
    effect(() => {
      const tema = this.tema();
      this.document.documentElement.classList.toggle('dark', tema === 'oscuro');
      this.guardar(tema);
    });
  }

  alternar(): void {
    this.tema.update((tema) => (tema === 'oscuro' ? 'claro' : 'oscuro'));
  }

  private leerTemaInicial(): Tema {
    const guardado = this.leerGuardado();
    if (guardado === 'dark') return 'oscuro';
    if (guardado === 'light') return 'claro';
    const prefiereOscuro = this.document.defaultView?.matchMedia?.('(prefers-color-scheme: dark)').matches;
    return prefiereOscuro ? 'oscuro' : 'claro';
  }

  // En localStorage se guarda 'dark' / 'light' para seguir siendo compatible con el front anterior.
  private leerGuardado(): string | null {
    try {
      return localStorage.getItem(CLAVE_TEMA);
    } catch {
      return null;
    }
  }

  private guardar(tema: Tema): void {
    try {
      localStorage.setItem(CLAVE_TEMA, tema === 'oscuro' ? 'dark' : 'light');
    } catch {
      // Sin acceso a localStorage (modo privado, por ejemplo): el tema funciona igual, solo no se recuerda.
    }
  }
}
