import { Routes } from '@angular/router';
import { soloInvitados, soloLogueados } from './guards/auth';
import { Cotizar } from './pages/cotizar/cotizar';
import { Login } from './pages/login/login';
import { Registro } from './pages/registro/registro';

export const routes: Routes = [
  { path: '', redirectTo: 'cotizar', pathMatch: 'full' },
  { path: 'login', component: Login, canActivate: [soloInvitados] },
  { path: 'registro', component: Registro, canActivate: [soloInvitados] },
  { path: 'cotizar', component: Cotizar, canActivate: [soloLogueados] },
  { path: '**', redirectTo: 'cotizar' },
];
