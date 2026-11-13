import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { AvisoService } from '../../services/avisos';

// Adonde vuelve el back después de entrar con Google (RF03): /oauth2/redirect?token=… (o ?error=… si falló).
// Guarda la sesión y sigue a cotizar. No se queda en esta dirección, así el token no queda a la vista
@Component({
  selector: 'app-oauth2-redirect',
  imports: [],
  template: '<p class="dato">Entrando con Google…</p>',
  styles: '.dato { margin: 2rem 1rem; text-align: center; color: var(--color-texto-suave); }',
})
export class Oauth2Redirect {
  private readonly auth = inject(AuthService);
  private readonly avisos = inject(AvisoService);
  private readonly router = inject(Router);

  constructor() {
    const token = inject(ActivatedRoute).snapshot.queryParamMap.get('token');

    // Sin token (el back mandó ?error=… o se entró a mano) no hay nada que guardar
    if (!token) {
      this.fallar();
      return;
    }

    // El token solo no dice el rol: se le pregunta al back de quién es. Si el back lo rechaza (un token
    // inventado o vencido), es el error que pide la historia
    this.auth.cuentaDeGoogle(token).subscribe({
      next: (usuario) => {
        this.auth.guardarSesion({ ...usuario, token });
        // replaceUrl: la dirección con el token se reemplaza, así no queda en el historial del navegador
        this.router.navigate(['/cotizar'], { replaceUrl: true });
      },
      error: () => this.fallar(),
    });
  }

  // Vuelve al login con el aviso de la historia, también sin dejar la dirección con el token en el historial
  private fallar() {
    this.avisos.mostrar('No se pudo completar el inicio con Google.', 'error');
    this.router.navigate(['/login'], { replaceUrl: true });
  }
}
