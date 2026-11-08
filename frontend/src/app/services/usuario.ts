import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { PerfilForm, Usuario } from '../models/usuario';
import { ViajeFrecuente, ViajeHistorial } from '../models/viaje';

// Pedidos sobre las cuentas de usuario: el perfil propio, su historial y, para el admin, buscar y eliminar
@Service()
export class UsuarioService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/usuarios`;

  // GET /usuarios/me → los datos de la cuenta con la que se inició sesión (el back la saca del token)
  actual() {
    return this.http.get<Usuario>(`${this.url}/me`);
  }

  // PUT /usuarios/:id → guarda los datos nuevos del perfil y devuelve la cuenta ya actualizada
  actualizar(id: number, datos: PerfilForm) {
    return this.http.put<Usuario>(`${this.url}/${id}`, datos);
  }

  // GET /usuarios/:id/historial → los viajes de la cuenta, del más reciente al más viejo (RF13)
  historial(id: number) {
    return this.http.get<ViajeHistorial[]>(`${this.url}/${id}/historial`);
  }

  // DELETE /usuarios/:id/historial/:viajeId → borra ese viaje del historial (RF14); no devuelve nada (void)
  borrarViaje(id: number, viajeId: number) {
    return this.http.delete<void>(`${this.url}/${id}/historial/${viajeId}`);
  }

  // GET /usuarios/:id/viaje-frecuente → el viaje más repetido (RF15). Si no hay, el back responde 204
  // sin cuerpo y llega null
  viajeFrecuente(id: number) {
    return this.http.get<ViajeFrecuente | null>(`${this.url}/${id}/viaje-frecuente`);
  }

  // GET /usuarios/buscar?query=… → el usuario con ese nombre o email (si no existe, el back da 404)
  buscar(query: string) {
    return this.http.get<Usuario>(`${this.url}/buscar`, { params: { query } });
  }

  // DELETE /usuarios/:id → borra la cuenta (la propia, o cualquiera si es el admin); no devuelve nada (void)
  eliminar(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
