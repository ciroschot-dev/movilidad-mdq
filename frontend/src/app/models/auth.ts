// Lo que mandamos para entrar (igual al LoginRequest del back)
export interface Credenciales {
  username: string;
  password: string;
}

// Lo que devuelve el back al entrar o registrarse (igual al AuthResponse)
export interface Sesion {
  id: number;
  username: string;
  email: string;
  token: string; // la "pulsera" que hay que mostrar en cada pedido
  role: 'USER' | 'ADMIN';
}
