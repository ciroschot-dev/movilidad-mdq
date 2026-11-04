import { Service } from '@angular/core';
import { Lugar } from '../models/lugar';

// Centro de Mar del Plata: Google prioriza las direcciones que están cerca de acá
const MAR_DEL_PLATA: google.maps.LatLngLiteral = { lat: -38.0055, lng: -57.5426 };

// Habla con Google: sugiere direcciones mientras se escribe, trae los datos de la elegida y arma la ruta.
// Google no devuelve Observables como HttpClient sino Promises: por eso los métodos son async y usan await.
@Service()
export class LugarService {
  // Agrupa todos los pedidos de una misma búsqueda, así Google la cobra como una sola
  private sesion: google.maps.places.AutocompleteSessionToken | null = null;

  // Las sugerencias para lo que se escribió hasta ahora (ej.: "Güemes 23" → "Güemes 23, Mar del Plata…")
  async sugerir(texto: string) {
    // await espera a que cargue la librería de Places (solo tarda la primera vez)
    const { AutocompleteSuggestion, AutocompleteSessionToken } =
      await google.maps.importLibrary('places');
    // ??= crea el token solo si no hay uno: se reusa hasta que se elige una dirección
    this.sesion ??= new AutocompleteSessionToken();

    const { suggestions } = await AutocompleteSuggestion.fetchAutocompleteSuggestions({
      input: texto,
      sessionToken: this.sesion,
      language: 'es',
      includedRegionCodes: ['ar'], // solo direcciones de Argentina
      locationBias: { center: MAR_DEL_PLATA, radius: 20000 }, // y primero las de 20 km alrededor
    });
    // Cada sugerencia trae la predicción de un lugar; se descartan las que vienen vacías
    return suggestions
      .map((sugerencia) => sugerencia.placePrediction)
      .filter((prediccion) => prediccion !== null);
  }

  // Trae los datos completos de la sugerencia elegida y cierra la búsqueda
  async elegir(prediccion: google.maps.places.PlacePrediction): Promise<Lugar> {
    const { place } = await prediccion.toPlace().fetchFields({
      fields: ['displayName', 'formattedAddress', 'location'],
    });
    // Con la dirección elegida termina la búsqueda: la próxima arranca con un token nuevo
    this.sesion = null;
    // Sin coordenadas no se puede marcar en el mapa: se trata como un error (lo atrapa el catch de cotizar)
    if (!place.location) {
      throw new Error('Google no devolvió la ubicación del lugar');
    }

    return {
      nombre: place.displayName ?? prediccion.mainText?.text ?? prediccion.text.text,
      direccion: place.formattedAddress ?? prediccion.text.text,
      placeId: prediccion.placeId,
      lat: place.location.lat(),
      lng: place.location.lng(),
    };
  }

  // Pide a Google el camino en auto entre las dos direcciones, para dibujarlo en el mapa
  async ruta(origen: Lugar, destino: Lugar) {
    const { DirectionsService, TravelMode } = await google.maps.importLibrary('routes');
    return new DirectionsService().route({
      origin: { placeId: origen.placeId },
      destination: { placeId: destino.placeId },
      travelMode: TravelMode.DRIVING,
    });
  }
}
