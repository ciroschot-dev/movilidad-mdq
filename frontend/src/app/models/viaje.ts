// Lo que se carga en el formulario de cotizar (los dos campos obligatorios de CalculoViajeRequest del back)
export interface BusquedaForm {
  origen: string;
  destino: string;
}

// Lo que se manda al back al cotizar (igual a CalculoViajeRequest): el texto de los campos más los datos
// de Google de cada dirección elegida, que el back usa para medir la distancia y armar el link de Uber
export interface CalculoViaje extends BusquedaForm {
  origenAddressLine1: string; // nombre corto
  origenAddressLine2: string; // dirección completa
  origenPlaceId: string;
  origenLat: number;
  origenLng: number;
  destinoAddressLine1: string;
  destinoAddressLine2: string;
  destinoPlaceId: string;
  destinoLat: number;
  destinoLng: number;
}

// Un viaje del historial (los campos de ViajeHistorialResponse que muestra la pantalla Historial, RF13)
export interface ViajeHistorial {
  id: number;
  origen: string;
  destino: string;
  distanciaEnMetros: number;
  tiempoEstimadoMin: number;
  // Los precios de aquel momento; null si esa app no se pudo cotizar
  precioTaxi: number | null;
  precioUber: number | null;
  precioDidi: number | null;
  fechaHora: string; // llega como aaaa-mm-ddThh:mm:ss
}

// Un destino del ranking de los más buscados (igual a DestinoPopularResponse): el lugar y cuántas veces se buscó
export interface DestinoPopular {
  destino: string;
  cantidad: number;
}

// Lo que se completa en el filtro del ranking (RF25): las dos fechas. Son opcionales
export interface FiltroDestinos {
  desde: string; // llegan como aaaa-mm-dd desde el input de fecha
  hasta: string;
}

// Una de las opciones que devuelve el back al cotizar (igual a OpcionTransporteResponse)
export interface OpcionTransporte {
  viajeId: number | null; // id del viaje guardado; null si el back no pudo guardarlo
  tipo: 'TAXI' | 'UBER' | 'DIDI';
  precio: number; // en pesos
  tiempoMinutos: number;
  distanciaEnMetros: number;
  url: string; // link para abrir Uber o Didi, o para llamar al taxi
}
