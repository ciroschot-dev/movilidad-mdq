import { Component } from '@angular/core';
import { Mapa } from '../../components/mapa/mapa';

// Pantalla provisoria: el registro necesita adónde llevar al usuario hasta que esté RF07
@Component({
  selector: 'app-cotizar',
  imports: [Mapa],
  template: '<h1>Cotizar</h1> <app-mapa />',
})
export class Cotizar {}
