// Un usuario tal como lo devuelve el back al buscarlo (igual al UsuarioResponse)
export interface Usuario {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
}
