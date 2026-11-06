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

// Para las pantallas del admin: si no sos ADMIN, te manda a cotizar
// (y si encima no tenés sesión, el guard de cotizar te lleva al login)
export const soloAdmin: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.esAdmin() ? true : router.createUrlTree(['/cotizar']);
};
