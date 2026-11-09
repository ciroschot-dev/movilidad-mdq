import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { DireccionFavorita } from '../models/lugar';
import { CalculoViaje, DestinoPopular, FiltroDestinos, OpcionTransporte } from '../models/viaje';

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

  // PUT /viajes/:viajeId/favorito → pone o saca la ⭐ (RF17). Al ponerla, el back guarda las dos direcciones
  // en Favoritos. No devuelve nada (void): el estado nuevo es el contrario al que tenía
  alternarFavorito(viajeId: number) {
    return this.http.put<void>(`${this.url}/${viajeId}/favorito`, null);
  }

  // GET /viajes/direcciones-favoritas → las direcciones guardadas en Favoritos (RF18). El back sabe de quién
  // son por el token, así que no lleva el id de la cuenta
  direccionesFavoritas() {
    return this.http.get<DireccionFavorita[]>(`${this.url}/direcciones-favoritas`);
  }

  // PUT /viajes/direcciones-favoritas/{id} → le cambia el nombre a una dirección guardada (RF27)
  renombrarDireccion(id: number, nombre: string) {
    return this.http.put<void>(`${this.url}/direcciones-favoritas/${id}`, { nombre });
  }

  // DELETE /viajes/direcciones-favoritas/{id} → saca una dirección de Favoritos (RF27)
  quitarDireccion(id: number) {
    return this.http.delete<void>(`${this.url}/direcciones-favoritas/${id}`);
  }

  // Top 10 de los destinos más buscados, filtrado por fechas (RF25). Solo manda las fechas que tienen algo
  destinosPopulares(filtro: FiltroDestinos) {
    let params = new HttpParams();
    // El input de fecha da solo el día; el back espera fecha y hora, así que cada día arranca a las 00:00
    // y el "hasta" cierra a las 23:59, para que las búsquedas de ese día entren completas
    if (filtro.desde) {
      params = params.set('desde', `${filtro.desde}T00:00:00`);
    }
    if (filtro.hasta) {
      params = params.set('hasta', `${filtro.hasta}T23:59:59`);
    }
    return this.http.get<DestinoPopular[]>(`${this.url}/admin/destinos-populares`, { params });
  }
}
