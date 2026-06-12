export interface AuthSession {
  id: number;
  username: string;
  email: string;
  token: string;
  role: 'USER' | 'ADMIN';
}

export interface DireccionFavorita {
  id: number;
  nombre: string | null;
  direccion: string;
  placeId: string;
  lat: number;
  lng: number;
}
