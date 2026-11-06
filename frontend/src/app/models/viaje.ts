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

// Una de las opciones que devuelve el back al cotizar (igual a OpcionTransporteResponse)
export interface OpcionTransporte {
  tipo: 'TAXI' | 'UBER' | 'DIDI';
  precio: number; // en pesos
  tiempoMinutos: number;
  distanciaEnMetros: number;
  url: string; // link para abrir Uber o Didi, o para llamar al taxi
}
