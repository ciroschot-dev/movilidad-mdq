// Lo que se carga en el formulario de cotizar (los dos campos obligatorios de CalculoViajeRequest del back)
export interface BusquedaForm {
  origen: string;
  destino: string;
}

// Una de las opciones que devuelve el back al cotizar (igual a OpcionTransporteResponse)
export interface OpcionTransporte {
  tipo: 'TAXI' | 'UBER' | 'DIDI';
  precio: number; // en pesos
  tiempoMinutos: number;
  distanciaEnMetros: number;
  url: string; // link para abrir Uber o Didi, o para llamar al taxi
}
