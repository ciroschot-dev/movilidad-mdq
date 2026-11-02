import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth';

// Pasa por acá CADA pedido que sale de la app, antes de llegar al back
export const authInterceptor: HttpInterceptorFn = (pedido, siguiente) => {
  const auth = inject(AuthService);
  const token = auth.token();

  // Si hay token, le pega una copia del pedido con el token en el encabezado
  const pedidoConToken = token
    ? pedido.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : pedido;

  return siguiente(pedidoConToken).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401 = el back no acepta el token (venció o es trucho): afuera, con aviso.
      // Solo si mandamos token: un 401 sin token es una contraseña mal en el login
      if (error.status === 401 && token) {
        auth.cerrarSesion(true);
      }
      // Igual se pasa el error, para que la pantalla pueda mostrar un mensaje
      return throwError(() => error);
    }),
  );
};
