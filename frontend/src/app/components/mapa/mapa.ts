import { Component, input } from '@angular/core';
import { GoogleMap, MapDirectionsRenderer } from '@angular/google-maps';

// Mapa de Google centrado en Mar del Plata; cuando cotizar le pasa una ruta, dibuja el camino
// con los marcadores A (origen) y B (destino) (RF08)
@Component({
  selector: 'app-mapa',
  imports: [GoogleMap, MapDirectionsRenderer],
  templateUrl: './mapa.html',
})
export class Mapa {
  // input: cotizar me pasa el camino entre origen y destino; null mientras falte elegir alguno
  ruta = input<google.maps.DirectionsResult | null>(null);

  // Centro de Mar del Plata, así el mapa arranca mostrando la ciudad
  centro: google.maps.LatLngLiteral = { lat: -38.0055, lng: -57.5426 };
  // Con zoom 12 se ve la ciudad entera
  zoom = 12;
}
