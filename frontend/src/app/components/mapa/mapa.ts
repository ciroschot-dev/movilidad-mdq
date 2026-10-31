import { Component } from '@angular/core';
import { GoogleMap } from '@angular/google-maps';

// Mapa de Google centrado en Mar del Plata; más adelante acá se dibujan el origen, el destino y la ruta (RF08)
@Component({
  selector: 'app-mapa',
  imports: [GoogleMap],
  templateUrl: './mapa.html',
})
export class Mapa {
  // Centro de Mar del Plata, así el mapa arranca mostrando la ciudad
  centro: google.maps.LatLngLiteral = { lat: -38.0055, lng: -57.5426 };
  // Con zoom 12 se ve la ciudad entera
  zoom = 12;
}
