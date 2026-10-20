import { Routes } from '@angular/router';
import { Cotizar } from './pages/cotizar/cotizar';
import { Registro } from './pages/registro/registro';

export const routes: Routes = [
  { path: '', redirectTo: 'cotizar', pathMatch: 'full' },
  { path: 'registro', component: Registro },
  { path: 'cotizar', component: Cotizar },
  { path: '**', redirectTo: 'cotizar' },
];
