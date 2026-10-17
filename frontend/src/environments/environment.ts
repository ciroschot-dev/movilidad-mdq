// Producción (Vercel). /api lo redirige vercel.json al backend en Render.
export const environment = {
  apiUrl: '/api',
  // El login con Google hace una redirección completa, así que va directo al backend y no por /api.
  oauthBaseUrl: 'https://movilidad-mdq-backend.onrender.com',
  // Clave de navegador: es pública, pero tiene que estar restringida por referer en Google Cloud.
  googleMapsApiKey: '',
};
