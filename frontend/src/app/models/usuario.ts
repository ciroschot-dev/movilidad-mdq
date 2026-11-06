// Un usuario tal como lo devuelve el back al buscarlo (igual al UsuarioResponse)
export interface Usuario {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

// Datos del formulario de perfil: los mismos campos que pide ActualizarUsuarioRequest en el back.
// La contraseña vacía significa "no cambiarla"
export interface PerfilForm {
  username: string;
  email: string;
  password: string;
}
