import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { RegistroForm } from '../models/registro';
import { Sesion } from '../models/sesion';

@Service()
export class Auth {
  private readonly http = inject(HttpClient);
  // /api lo redirige al backend vercel.json en producción y proxy.conf.json en local
  private readonly url = '/api/usuarios';

  registrar(datos: RegistroForm) {
    return this.http.post<Sesion>(`${this.url}/registro`, datos);
  }

  // Se guarda en el navegador para que la sesión siga abierta al recargar
  guardarSesion(sesion: Sesion) {
    localStorage.setItem('sesion', JSON.stringify(sesion));
  }
}
