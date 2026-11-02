import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

// Para las pantallas de adentro (cotizar, perfil…): sin sesión, te manda al login
export const soloLogueados: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.estaLogueado() ? true : router.createUrlTree(['/login']);
};

// Para login y registro: si ya tenés sesión guardada, entrás directo a cotizar
export const soloInvitados: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.estaLogueado() ? router.createUrlTree(['/cotizar']) : true;
};
