import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { BusquedaForm, OpcionTransporte } from '../models/viaje';

// Junta los pedidos al back que tienen que ver con viajes (/viajes)
@Service()
export class ViajeService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/viajes`;

  // Manda origen y destino; el back devuelve Taxi, Uber y Didi, y guarda el viaje en el historial
  calcular(busqueda: BusquedaForm) {
    return this.http.post<OpcionTransporte[]>(`${this.url}/calcular`, busqueda);
  }
}
