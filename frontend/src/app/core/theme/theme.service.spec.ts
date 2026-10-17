import { TestBed } from '@angular/core/testing';
import { CLAVE_TEMA, ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark');
  });

  it('usa el tema guardado', () => {
    localStorage.setItem(CLAVE_TEMA, 'dark');
    const service = TestBed.inject(ThemeService);
    expect(service.tema()).toBe('oscuro');
  });

  it('al alternar cambia la clase del html y guarda la preferencia', () => {
    localStorage.setItem(CLAVE_TEMA, 'light');
    const service = TestBed.inject(ThemeService);

    service.alternar();
    TestBed.tick();

    expect(service.tema()).toBe('oscuro');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
    expect(localStorage.getItem(CLAVE_TEMA)).toBe('dark');
  });
});
