# 🚗 MovilidadMDQ

App academica para comparar opciones de transporte en Mar del Plata. Calcula alternativas de **Taxi**, **Uber** y **Didi**, guarda historial de viajes por usuario en **AWS RDS** y usa **Spring Security con JWT + Google OAuth2**.

> 📘 Para entender que se cambio y como funciona la app por dentro, leer `docs/EXPLICACION_CAMBIOS.md`.

## 🧭 Mapa Rapido

| Parte | Tecnologia | Carpeta |
| --- | --- | --- |
| Backend | Java 21, Spring Boot 4, Security, JPA | `src/main/java` |
| Frontend actual | React 19, TypeScript, Vite | `frontend/` |
| Frontend viejo | HTML/JS demo legado | `frontend-demo/` |
| Base de datos | MySQL en AWS RDS | Configurada por `.env` |
| APIs externas | Google Maps, Google Places, OpenWeather | Configuradas por `.env` |

✅ Usar `frontend/` para ejecutar la app actual.

⚠️ `frontend-demo/` queda solo como referencia historica.

## ✅ Antes De Empezar

Necesitas tener:

- Java 21.
- Node.js y npm.
- Acceso a la base MySQL de AWS RDS.
- API key de Google Maps.
- API key de OpenWeather.
- OAuth Client de Google si vas a probar `Continuar con Google`.

## 🔐 1. Configurar Backend

Crea un archivo `.env` en la raiz del proyecto usando `.env.example` como guia.

```env
GOOGLE_MAPS_KEY=tu_google_maps_key
WEATHER_API_KEY=tu_openweather_key

SPRING_DATASOURCE_URL=jdbc:mysql://tu-rds.amazonaws.com:3306/movilidadmdq
DB_USER=tu_usuario
DB_PASSWORD=tu_password

JWT_SECRET=clave_base64_de_32_bytes_o_mas
JWT_EXPIRATION=86400000

GOOGLE_OAUTH_CLIENT_ID=tu_client_id_google
GOOGLE_OAUTH_CLIENT_SECRET=tu_client_secret_google
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Para generar `JWT_SECRET`:

```bash
openssl rand -base64 32
```

📌 El backend lee `.env` automaticamente desde `application.properties`.

## 🖥️ 2. Configurar Frontend

Crea `frontend/.env` usando `frontend/.env.example` como guia.

```env
VITE_API_URL=http://localhost:8080
VITE_GOOGLE_MAPS_API_KEY=tu_google_maps_key_para_browser
```

📌 La key del frontend necesita **Maps JavaScript API** y **Places API**.

📌 En Google Cloud conviene restringirla a:

```text
http://localhost:5173/*
```

## 🔵 3. Configurar Google OAuth2

En Google Cloud Console, dentro del OAuth Client Web, agregar:

**Authorized redirect URI**

```text
http://localhost:8080/login/oauth2/code/google
```

**Authorized JavaScript origins**

```text
http://localhost:5173
http://localhost:8080
```

⚠️ Los origins no llevan path. Solo protocolo, host y puerto.

Luego el flujo vuelve al frontend en:

```text
http://localhost:5173/oauth2/redirect?token=...
```

## ▶️ 4. Ejecutar La App

Terminal 1, backend:

```bash
./mvnw spring-boot:run
```

Terminal 2, frontend:

```bash
cd frontend && npm install && npm run dev
```

Abrir en el navegador:

```text
http://localhost:5173
```

## 🧪 5. Probar La App

Checklist recomendado:

- ✅ Abrir `http://localhost:5173`.
- ✅ Registrarse con usuario, email y contrasena.
- ✅ Cerrar sesion y volver a iniciar sesion.
- ✅ Probar `Continuar con Google`.
- ✅ Calcular un viaje en Mar del Plata.
- ✅ Ver opciones de Taxi, Uber y Didi.
- ✅ Entrar a `Historial` y confirmar que se guardo el viaje.

## 🔌 Endpoints Principales

| Metodo | Endpoint | Para que sirve |
| --- | --- | --- |
| `POST` | `/usuarios/registro` | Crear usuario y devolver JWT |
| `POST` | `/usuarios/login` | Login clasico y devolver JWT |
| `GET` | `/usuarios/me` | Obtener usuario actual desde JWT |
| `POST` | `/viajes/calcular` | Calcular viaje y guardar historial |
| `GET` | `/usuarios/{id}/historial` | Ver historial del usuario autenticado |
| `PUT` | `/usuarios/{id}` | Actualizar perfil propio |

Los endpoints protegidos necesitan:

```text
Authorization: Bearer TU_TOKEN
```

## 🛠️ 6. Verificar Que Todo Compile

Backend:

```bash
./mvnw test
```

Frontend:

```bash
cd frontend && npm run build
```

## 🧯 Problemas Comunes

| Problema | Que revisar |
| --- | --- |
| No encuentra Java | Instalar Java 21 |
| Error 401 al calcular | Falta iniciar sesion o vencio el token |
| Google Maps no carga | `VITE_GOOGLE_MAPS_API_KEY` en `frontend/.env` |
| Distance Matrix falla | `GOOGLE_MAPS_KEY` en `.env` raiz |
| OAuth2 falla | Redirect URI y JavaScript origins en Google Cloud |
| No conecta a AWS | `SPRING_DATASOURCE_URL`, `DB_USER`, `DB_PASSWORD` y seguridad de RDS |

## 🧠 Notas Para El Equipo

- 🔒 No commitear `.env` ni claves reales.
- ✅ `frontend/` es la app vigente.
- 🕰️ `frontend-demo/` es legado y no se usa para ejecutar la app actual.
- 🚀 En produccion, restringir CORS y API keys al dominio real.
- 📚 Si queres entender todo el cambio, leer `docs/EXPLICACION_CAMBIOS.md`.
