import { Component, computed, inject } from '@angular/core';
import { ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'app-theme-toggle',
  template: `
    <button
      type="button"
      (click)="theme.alternar()"
      [attr.aria-label]="etiqueta()"
      [title]="etiqueta()"
      class="boton-tema"
    >
      @if (esOscuro()) {
        <!-- Sol -->
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <circle cx="12" cy="12" r="4" />
          <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41" />
        </svg>
      } @else {
        <!-- Luna -->
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z" />
        </svg>
      }
    </button>
  `,
  styles: `
    .boton-tema {
      display: inline-flex;
      width: 2.5rem;
      height: 2.5rem;
      align-items: center;
      justify-content: center;
      border: 1px solid var(--color-border);
      border-radius: 9999px;
      background-color: var(--color-surface);
      color: var(--color-text-muted);
      cursor: pointer;
      transition: background-color 150ms;
    }

    .boton-tema:hover {
      background-color: var(--color-surface-hover);
    }

    svg {
      width: 1.25rem;
      height: 1.25rem;
    }
  `,
})
export class ThemeToggle {
  protected readonly theme = inject(ThemeService);

  protected readonly esOscuro = computed(() => this.theme.tema() === 'oscuro');
  protected readonly etiqueta = computed(() => (this.esOscuro() ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'));
}
