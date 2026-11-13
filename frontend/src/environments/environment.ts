// Producción (Vercel): /api lo redirige vercel.json al backend en Render
export const environment = {
  apiUrl: '/api',
  // El login con Google va directo al back, sin pasar por /api: Google vuelve a la dirección del back
  // (la que está cargada en Google Cloud) y ahí el back tiene que encontrar su propia sesión
  backendUrl: 'https://movilidad-mdq-backend.onrender.com',
};
