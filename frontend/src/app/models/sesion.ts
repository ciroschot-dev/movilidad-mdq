// Lo que devuelve el backend al registrarse o iniciar sesión (AuthResponse)
export interface Sesion {
  id: number;
  username: string;
  email: string;
  token: string;
  role: 'USER' | 'ADMIN';
}
