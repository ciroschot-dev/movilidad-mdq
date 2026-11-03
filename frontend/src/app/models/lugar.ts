// Una dirección elegida de la lista de Google: lo que el back necesita para ubicarla bien
export interface Lugar {
  nombre: string; // nombre corto (ej.: "Güemes 23" o "Grand Beach")
  direccion: string; // dirección completa (ej.: "Güemes 23, Mar del Plata, Provincia de Buenos Aires, Argentina")
  placeId: string; // el código con el que Google identifica ese lugar
  lat: number;
  lng: number;
}
