export interface AuthSession {
  id: number;
  username: string;
  email: string;
  token: string;
  role: 'USER' | 'ADMIN';
}
