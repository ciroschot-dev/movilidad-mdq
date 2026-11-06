import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { Tarifa } from '../models/tarifa';

// Pedidos del admin sobre la tarifa del taxi: ver la de hoy y guardar la nueva
@Service()
export class TarifaService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/tarifas/taxi`;

  // GET /admin/tarifas/taxi → la tarifa vigente, para llenar el formulario
  obtener() {
    return this.http.get<Tarifa>(this.url);
  }

  // PUT /admin/tarifas/taxi → la guarda y devuelve la tarifa ya actualizada
  actualizar(tarifa: Tarifa) {
    return this.http.put<Tarifa>(this.url, tarifa);
  }
}
