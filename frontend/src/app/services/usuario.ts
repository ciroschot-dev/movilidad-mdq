import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { Usuario } from '../models/usuario';

// Pedidos del admin sobre las cuentas de usuario: buscar una y eliminarla
@Service()
export class UsuarioService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/usuarios`;

  // GET /usuarios/buscar?query=… → el usuario con ese nombre o email (si no existe, el back da 404)
  buscar(query: string) {
    return this.http.get<Usuario>(`${this.url}/buscar`, { params: { query } });
  }

  // DELETE /usuarios/:id → borra la cuenta; no devuelve nada (void)
  eliminar(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
