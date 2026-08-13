# Deploy independiente: Render + Neon

Esta guía deja el backend siempre activo en Render (Docker), con PostgreSQL
gratuito en Neon y HTTPS automático. El frontend continúa en Vercel.

## Arquitectura

```text
Vercel (frontend React)
  -> HTTPS
Render (backend Docker)
  -> Spring Boot :8080
  -> Neon PostgreSQL (serverless)
```

- Backend: Render Web Service (free tier, Docker).
- Base de datos: Neon PostgreSQL (free tier, serverless, scale-to-zero).
- Frontend: Vercel (sin cambios respecto al deploy actual).
- UptimeRobot: ping cada 5 minutos para evitar que Render duerma el servicio.

## 1. Crear la base de datos en Neon

1. Registrarse en [neon.tech](https://neon.tech) con GitHub.
2. Crear un proyecto llamado `movilidad-mdq`.
3. Crear una base de datos llamada `movilidadmdq` (sin guiones).
4. Copiar el connection string que te da Neon. Tiene esta forma:

```text
postgresql://usuario:password@ep-xxx.us-east-2.aws.neon.tech/movilidadmdq?sslmode=require
```

5. Convertirlo a formato JDBC para Spring Boot:

```text
jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech:5432/movilidadmdq?sslmode=require
```

> Neon usa scale-to-zero: si no hay queries por 5 minutos, suspende el compute.
> Cuando llega una query, resume en ~200ms. Esto es transparente para la app.

## 2. Crear el servicio en Render

1. Registrarse en [render.com](https://render.com) con GitHub.
2. Ir a **New +** → **Web Service**.
3. Conectar el repositorio `ciroschot-dev/movilidad-mdq`.
4. Configurar:

| Campo | Valor |
|-------|-------|
| Name | `movilidad-mdq-backend` |
| Region | Oregon (US West) o el más cercano |
| Branch | `main` |
| Runtime | **Docker** |
| Instance Type | **Free** |

5. En **Environment Variables**, agregar todas las variables del `.env.example`:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx.neon.tech:5432/movilidadmdq?sslmode=require
DB_USER=usuario_neon
DB_PASSWORD=password_neon
JWT_SECRET=clave_base64_de_32_bytes_o_mas
JWT_EXPIRATION=86400000
GOOGLE_MAPS_KEY=tu_google_maps_key
WEATHER_API_KEY=tu_openweather_key
GOOGLE_OAUTH_CLIENT_ID=tu_client_id
GOOGLE_OAUTH_CLIENT_SECRET=tu_client_secret
APP_CORS_ALLOWED_ORIGINS=https://movilidad-mdq.vercel.app,http://localhost:5173
APP_OAUTH2_REDIRECT_URI=https://movilidad-mdq.vercel.app/oauth2/redirect
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=password_segura
```

6. Click en **Create Web Service**. Render construye la imagen Docker y despliega.

> Render asigna automáticamente una URL HTTPS tipo `https://movilidad-mdq-backend.onrender.com`.

## 3. Configurar UptimeRobot

El free tier de Render duerme el servicio tras 15 minutos sin tráfico.
UptimeRobot lo mantiene despierto con un ping periódico.

1. Registrarse en [uptimerobot.com](https://uptimerobot.com) (gratis).
2. Crear un monitor:
   - Type: **HTTP(s)**
   - URL: `https://movilidad-mdq-backend.onrender.com/api-docs`
   - Monitoring Interval: **5 minutes**

## 4. Configurar Google OAuth

En Google Cloud Console → APIs y servicios → Credenciales, agregar:

**Authorized redirect URI:**

```text
https://movilidad-mdq-backend.onrender.com/login/oauth2/code/google
```

**Authorized JavaScript origins:**

```text
https://movilidad-mdq.vercel.app
https://movilidad-mdq-backend.onrender.com
```

## 5. Configurar Vercel

En el dashboard de Vercel, actualizar las variables de entorno para Production:

```env
VITE_API_URL=https://movilidad-mdq-backend.onrender.com
VITE_OAUTH_BASE_URL=https://movilidad-mdq-backend.onrender.com
VITE_GOOGLE_MAPS_API_KEY=clave_maps_frontend
```

Hacer un redeploy de Vercel **únicamente después** de validar el backend nuevo.

## 6. Actualizar frontend

En `frontend/vercel.json` y `frontend/src/App.tsx`, reemplazar la URL del backend
por la URL real de Render. Commitear y pushear.

## 7. Validación final

- Swagger y `/api-docs` responden mediante HTTPS.
- Funcionan registro, login, OAuth, Maps, clima, cálculo, historial y admin.
- Los datos se persisten en Neon tras reinicio del servicio en Render.
- El monitor de UptimeRobot muestra el servicio como "Up".

## Limitaciones del free tier

| Recurso | Límite |
|---------|--------|
| Render backend | 750 horas/mes (suficiente para 24/7), se duerme tras 15 min sin tráfico |
| Neon PostgreSQL | 0.5 GB de storage, scale-to-zero tras 5 min idle |
| UptimeRobot | 50 monitores, intervalo mínimo 5 min |

Para un proyecto universitario, estos límites son más que suficientes.

## Separación de equipos

El otro grupo deberá:

1. Hacer fork del repositorio.
2. Crear su propio proyecto en Render (o cualquier hosting).
3. Crear su propia base de datos.
4. Crear sus propias credenciales de Google OAuth, Maps y OpenWeather.
5. Configurar su propio proyecto Vercel.

Cuando confirmen, se quitará acceso de escritura a los integrantes que no
pertenezcan al equipo de Ciro y Tiago.
