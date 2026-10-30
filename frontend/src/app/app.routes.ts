import { Routes } from '@angular/router';
import { Cotizar } from './pages/cotizar/cotizar';
import { Favoritos } from './pages/favoritos/favoritos';
import { Historial } from './pages/historial/historial';
import { Registro } from './pages/registro/registro';

// Tabla de rutas: cada URL muestra una pantalla
export const routes: Routes = [
  // Si la URL está vacía, arranca en cotizar, que es la pantalla principal
  { path: '', redirectTo: 'cotizar', pathMatch: 'full' },
  { path: 'registro', component: Registro },
  { path: 'cotizar', component: Cotizar },
  { path: 'historial', component: Historial },
  { path: 'favoritos', component: Favoritos },
  // Cualquier URL que no existe vuelve a cotizar. Va última porque atrapa todo
  { path: '**', redirectTo: 'cotizar' },
];
