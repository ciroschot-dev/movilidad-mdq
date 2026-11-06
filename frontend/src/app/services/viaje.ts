import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { CalculoViaje, OpcionTransporte } from '../models/viaje';

// Junta los pedidos al back que tienen que ver con viajes (/viajes)
@Service()
export class ViajeService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/viajes`;

  // Manda origen y destino con sus datos de Google; el back devuelve Taxi, Uber y Didi, y guarda el viaje en el historial
  calcular(busqueda: CalculoViaje) {
    return this.http.post<OpcionTransporte[]>(`${this.url}/calcular`, busqueda);
  }

  // Le avisa al back qué opción tocó el usuario, para que quede en el historial (RF10)
  elegir(viajeId: number, tipo: OpcionTransporte['tipo']) {
    return this.http.put<void>(`${this.url}/${viajeId}/opcion-elegida`, { tipo });
  }
}
