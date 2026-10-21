import { HttpClient } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { Credenciales, Sesion } from '../models/auth';
import { RegistroForm } from '../models/registro';

// Nombre de la "caja" del navegador donde queda guardado el token
const CLAVE_TOKEN = 'token';

@Service()
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private url = `${environment.apiUrl}/usuarios`;

  // Arranca con el token guardado: si recargás la página, seguís adentro
  token = signal<string | null>(localStorage.getItem(CLAVE_TOKEN));

  // Se recalcula solo cada vez que cambia el token
  estaLogueado = computed(() => this.token() !== null);

  // Le manda usuario y contraseña al back; el que llama se suscribe y recibe la sesión
  login(credenciales: Credenciales) {
    return this.http.post<Sesion>(`${this.url}/login`, credenciales);
  }

  // Crea la cuenta; el back ya devuelve la sesión, así que queda logueado sin pasar por el login
  registrar(datos: RegistroForm) {
    return this.http.post<Sesion>(`${this.url}/registro`, datos);
  }

  // Se llama después de un login o registro exitoso
  guardarSesion(sesion: Sesion) {
    localStorage.setItem(CLAVE_TOKEN, sesion.token);
    this.token.set(sesion.token);
  }

  // Borra el token y manda a la pantalla de login
  cerrarSesion() {
    localStorage.removeItem(CLAVE_TOKEN);
    this.token.set(null);
    this.router.navigate(['/login']);
  }
}
