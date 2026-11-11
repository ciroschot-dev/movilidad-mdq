import { HttpClient } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { Credenciales, Sesion } from '../models/auth';
import { RegistroForm } from '../models/registro';
import { Usuario } from '../models/usuario';

// Nombre de la "caja" del navegador donde queda guardado el token
const CLAVE_TOKEN = 'token';
// Y donde queda guardado el rol (USER o ADMIN), así no se pierde al recargar
const CLAVE_ROL = 'rol';

@Service()
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private url = `${environment.apiUrl}/usuarios`;

  // Arranca con el token guardado: si recargás la página, seguís adentro
  token = signal<string | null>(localStorage.getItem(CLAVE_TOKEN));

  // Se recalcula solo cada vez que cambia el token
  estaLogueado = computed(() => this.token() !== null);

  // El rol que mandó el back al entrar; arranca con el guardado, igual que el token
  rol = signal<string | null>(localStorage.getItem(CLAVE_ROL));

  // Solo es admin si el back dijo ADMIN: el nombre de usuario no cuenta (un USER llamado "admin" no entra)
  esAdmin = computed(() => this.rol() === 'ADMIN');

  // Queda en true cuando el back rechazó el token, así el login muestra el aviso
  sesionVencida = signal(false);

  // Le manda usuario y contraseña al back; el que llama se suscribe y recibe la sesión
  login(credenciales: Credenciales) {
    return this.http.post<Sesion>(`${this.url}/login`, credenciales);
  }

  // Crea la cuenta; el back ya devuelve la sesión, así que queda logueado sin pasar por el login
  registrar(datos: RegistroForm) {
    return this.http.post<Sesion>(`${this.url}/registro`, datos);
  }

  // La dirección del back que arranca el login con Google (RF03). Se abre en la misma pestaña: Google pide
  // la cuenta y, si autoriza, el back vuelve a /oauth2/redirect con el token en la URL
  urlGoogle = `${environment.backendUrl}/oauth2/authorization/google`;

  // Con el token que dejó el back en la URL después de Google, pide la cuenta (GET /usuarios/me) para saber
  // el rol. El token va a mano en el encabezado porque todavía no está guardado como sesión
  cuentaDeGoogle(token: string) {
    return this.http.get<Usuario>(`${this.url}/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });
  }

  // Se llama después de un login o registro exitoso
  guardarSesion(sesion: Sesion) {
    localStorage.setItem(CLAVE_TOKEN, sesion.token);
    localStorage.setItem(CLAVE_ROL, sesion.role);
    this.token.set(sesion.token);
    this.rol.set(sesion.role);
    this.sesionVencida.set(false);
  }

  // Borra el token y manda a la pantalla de login.
  // vencida = true cuando la saca el interceptor, no cuando el usuario toca "Cerrar sesión"
  cerrarSesion(vencida = false) {
    localStorage.removeItem(CLAVE_TOKEN);
    localStorage.removeItem(CLAVE_ROL);
    this.token.set(null);
    this.rol.set(null);
    this.sesionVencida.set(vencida);
    this.router.navigate(['/login']);
  }
}
