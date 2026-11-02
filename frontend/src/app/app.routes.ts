import { Routes } from '@angular/router';
import { soloInvitados, soloLogueados } from './guards/auth';
import { Cotizar } from './pages/cotizar/cotizar';
import { Favoritos } from './pages/favoritos/favoritos';
import { Historial } from './pages/historial/historial';
import { Login } from './pages/login/login';
import { Registro } from './pages/registro/registro';

// Tabla de rutas: cada URL muestra una pantalla
export const routes: Routes = [
  // Si la URL está vacía, arranca en cotizar, que es la pantalla principal
  { path: '', redirectTo: 'cotizar', pathMatch: 'full' },
  { path: 'login', component: Login, canActivate: [soloInvitados] },
  { path: 'registro', component: Registro, canActivate: [soloInvitados] },
  { path: 'cotizar', component: Cotizar, canActivate: [soloLogueados] },
  { path: 'historial', component: Historial, canActivate: [soloLogueados] },
  { path: 'favoritos', component: Favoritos, canActivate: [soloLogueados] },
  // Cualquier URL que no existe vuelve a cotizar. Va última porque atrapa todo
  { path: '**', redirectTo: 'cotizar' },
];
