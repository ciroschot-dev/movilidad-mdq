# 🚗 MovilidadMDQ — Frontend

App web de **MovilidadMDQ** construida con **React 19 + TypeScript + Vite**. Consume la API REST del backend Spring
Boot.

> 📘 La documentación general del proyecto (descripción, integrantes, backend, deploy, endpoints, ejemplos) está en
> el [README raíz](../README.md).

---

## 🌐 Deploy

- **Frontend** → https://movilidad-mdq.vercel.app/ (Vercel)

---

## 🛠️ Stack

- React 19
- TypeScript
- Vite
- React Router
- Google Maps JavaScript API + Places

---

## ✅ Requisitos

- Node.js 18+
- npm
- API key de Google Maps con **Maps JavaScript API** y **Places API** habilitadas

---

## ⚙️ Configuración

Copiá `.env.example` a `.env` y completá:

```env
VITE_API_URL=http://localhost:8080
VITE_GOOGLE_MAPS_API_KEY=tu_google_maps_key_para_browser
```

> Para apuntar al backend de producción, usar `VITE_API_URL=https://movilidadmdq.ddns.net`.

> En Google Cloud conviene restringir la key por referer a `http://localhost:5173/*` y al dominio del deploy.

---

## ▶️ Cómo correrlo

```bash
npm install
npm run dev
```

Abre `http://localhost:5173`.

---

## 📦 Build de producción

```bash
npm run build
npm run preview   # opcional, sirve el build localmente
```

---

## 📂 Estructura

```text
frontend/
├── public/           # Assets estáticos
├── src/              # Componentes, páginas, hooks, API client
├── index.html
├── vite.config.ts
├── tsconfig*.json
├── eslint.config.js
├── vercel.json       # Config de deploy en Vercel
└── .env.example
```

---

## 🚀 Deploy en Vercel

Está configurado mediante `vercel.json`. En el dashboard de Vercel, las variables que hay que definir son las mismas del
`.env`:

- `VITE_API_URL` → `https://movilidadmdq.ddns.net`
- `VITE_GOOGLE_MAPS_API_KEY` → la key de Google Maps (con el dominio de Vercel permitido)

---

## 🧯 Problemas comunes

| Problema                 | Qué revisar                                                          |
|--------------------------|----------------------------------------------------------------------|
| Mapa no carga            | `VITE_GOOGLE_MAPS_API_KEY` en `.env` y restricciones en Google Cloud |
| Llamadas a la API fallan | `VITE_API_URL` correcto y backend levantado                          |
| CORS bloqueado           | `APP_CORS_ALLOWED_ORIGINS` en el `.env` del backend                  |
| 401 al calcular viaje    | Falta JWT o el token venció — iniciar sesión otra vez                |
