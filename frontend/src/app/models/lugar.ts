// Una dirección elegida de la lista de Google: lo que el back necesita para ubicarla bien
export interface Lugar {
  nombre: string; // nombre corto (ej.: "Güemes 23" o "Grand Beach")
  direccion: string; // dirección completa (ej.: "Güemes 23, Mar del Plata, Provincia de Buenos Aires, Argentina")
  placeId: string; // el código con el que Google identifica ese lugar
  lat: number;
  lng: number;
}

// Una dirección guardada en Favoritos (igual a DireccionFavoritaResponse, RF18). El back la guarda al marcar
// un viaje con la ⭐; el nombre queda vacío hasta que el usuario lo renombra (RF27)
export interface DireccionFavorita {
  id: number;
  nombre: string | null;
  direccion: string;
  placeId: string | null;
  lat: number | null;
  lng: number | null;
}
