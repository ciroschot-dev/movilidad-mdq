# 🚗 MovilidadMDQ

> Aplicación académica que compara opciones de transporte en Mar del Plata (Taxi, Uber y Didi), guarda el historial de
> viajes por usuario y permite iniciar sesión con email/contraseña o con Google.

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-AWS%20RDS-4479A1?logo=mysql&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger&logoColor=black)

---

## 📑 Índice

- [Cómo probar la app en 1 minuto](#-cómo-probar-la-app-en-1-minuto)
- [Descripción general](#-descripción-general)
- [Integrantes del grupo](#-integrantes-del-grupo)
- [Tecnologías utilizadas](#-tecnologías-utilizadas)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Entidades principales](#-entidades-principales)
- [Cómo ejecutar el proyecto en local](#-cómo-ejecutar-el-proyecto-en-local)
- [Autenticación y autorización](#-autenticación-y-autorización)
- [Endpoints](#-endpoints)
- [Ejemplos de requests y responses](#-ejemplos-de-requests-y-responses)
- [Documentación navegable (Swagger UI)](#-documentación-navegable-swagger-ui)
- [Verificar que todo compile](#-verificar-que-todo-compile)
- [Problemas comunes](#-problemas-comunes)
- [Notas para el equipo](#-notas-para-el-equipo)

---

## ✅ Cómo probar la app en 1 minuto

| Recurso                                     | Enlace                                                                                                                       |
|---------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| 🌐 **Frontend desplegado (Vercel)**         | https://movilidad-mdq.vercel.app/                                                                                            |
| 🛰️ **Backend desplegado (AWS EC2)**        | https://movilidadmdq.ddns.net *(ver nota abajo)*                                                                             |
| 📘 **Documentación de la API (Swagger UI)** | https://movilidadmdq.ddns.net/swagger-ui.html ← **punto de entrada del backend**                                             |
| 📄 **OpenAPI JSON**                         | https://movilidadmdq.ddns.net/api-docs                                                                                       |
| 🧪 **Cómo crear un usuario de prueba**      | `POST /usuarios/registro` con `{ "username": "...", "password": "...", "email": "..." }` o el botón **Registrarse** en la UI |

**Aclaraciones importantes**:

- **No hace falta levantar nada local** para probar la app: el frontend (Vercel) ya está conectado al backend (EC2 con
  HTTPS). Solo entrá a https://movilidad-mdq.vercel.app/ y registrate.
- ℹ️ Si abrís https://movilidadmdq.ddns.net en el navegador vas a ver una **"Whitelabel Error Page" con status 401** —
  eso es **esperado**: el backend no tiene mapeo en la ruta raíz y Spring Security la protege. Para explorar la API usá
  **Swagger UI** (link de arriba) o pegale directo a un endpoint específico (ej. `/usuarios/login`).
- Si querés correr todo en local de todas formas, seguí los pasos
  de [Cómo ejecutar el proyecto](#-cómo-ejecutar-el-proyecto-en-local).
- La carpeta `frontend/` es la app vigente; cualquier referencia a `frontend-demo/` es legado y no se usa.
- No se incluyen credenciales hardcodeadas: el corrector puede registrar un usuario nuevo en segundos desde la UI o vía
  `curl`.

**Checklist funcional para validar la app**:

- ✅ Abrir https://movilidad-mdq.vercel.app/ (o `http://localhost:5173` si la corrés en local).
- ✅ Registrarse con usuario, email y contraseña.
- ✅ Cerrar sesión y volver a iniciar sesión.
- ✅ Probar **"Continuar con Google"** (OAuth2).
- ✅ Calcular un viaje en Mar del Plata.
- ✅ Ver opciones de Taxi, Uber y Didi.
- ✅ Entrar a **Historial** y confirmar que se guardó el viaje.
- ✅ Borrar un viaje del historial.

---

## 📖 Descripción general

**MovilidadMDQ** es una aplicación web pensada para usuarios que necesitan moverse por **Mar del Plata** y quieren
comparar de un vistazo cuánto les sale ir del punto A al B en **Taxi**, **Uber** o **Didi**.

La app combina:

- **Cálculo de distancia y tiempo** usando la **Google Distance Matrix API**.
- **Autocompletado de direcciones** con **Google Places**.
- **Tarifa de taxi oficial** parametrizable desde la base de datos (precio base + precio por km).
- **Estimación de precios** para apps de movilidad (Uber/Didi) con rangos mínimo y máximo.
- **Clima actual** del destino con **OpenWeather**.
- **Historial personal de viajes**, guardado por usuario en una base **MySQL en AWS RDS**.
- **Autenticación dual**: registro/login clásico con JWT + opción **"Continuar con Google"** (OAuth2).

Está pensada como proyecto académico para la materia **Programación 3 (UTN)**, pero el código sigue prácticas reales de
un backend Spring Boot moderno y un frontend React + TypeScript.

---

## 👥 Integrantes del grupo

| Integrante       | GitHub                                                                     |
|------------------|----------------------------------------------------------------------------|
| Ciro Schot       | [@ciroschot-dev](https://github.com/ciroschot-dev)                         |
| Morena Hidalgo   | [@morehidalgg0](https://github.com/morehidalgg0)                           |
| Anibal Bustos    | [@anibaldb](https://github.com/anibaldb)                                   |
| Franco Bavaresco | [@FrancoBavaresco](https://github.com/FrancoBavaresco)                     |
| Tiago Fueyo      | [@tiagofueyovuillermoz-beep](https://github.com/tiagofueyovuillermoz-beep) |

---

## 🛠️ Tecnologías utilizadas

**Backend**

- Java 21
- Spring Boot 4 (Web, Data JPA, Validation)
- Spring Security + JWT (`jjwt`)
- Spring Security OAuth2 Client (Google)
- springdoc-openapi (Swagger UI)
- Maven (con wrapper `./mvnw`)

**Frontend**

- React 19
- TypeScript
- Vite
- React Router

**Base de datos**

- MySQL 8 (alojada en AWS RDS)

**APIs externas**

- Google Maps (Distance Matrix, Places, Maps JavaScript)
- OpenWeather
- Google OAuth2 (Identity)

**Infraestructura / DevOps**

- Docker + `docker-compose.yml` (opcional para correr el backend en contenedor)
- Despliegue del frontend en **Vercel**

---

## 📂 Estructura del proyecto

```text
movilidadMDQ/
├── src/
│   └── main/
│       ├── java/com/example/movilidadmdq/
│       │   ├── config/         # Configuración de Spring, CORS, beans
│       │   ├── controller/     # Endpoints REST (Usuario, Viaje, Tarifa)
│       │   ├── dto/            # Records de request/response
│       │   ├── enums/          # Role, TipoTransporte
│       │   ├── exception/      # Manejo global de errores
│       │   ├── model/          # Entidades JPA: Usuario, Viaje, Tarifa
│       │   ├── repository/     # Spring Data JPA
│       │   ├── security/       # JWT filter, UserDetailsService, OAuth2 handler
│       │   └── service/        # Lógica de negocio
│       └── resources/
│           └── application.properties
├── frontend/                   # App React + TS + Vite (la que se usa)
├── schema.sql                  # Script DDL inicial + datos de ejemplo
├── Dockerfile                  # Imagen del backend
├── docker-compose.yml          # Orquestación opcional
├── pom.xml                     # Maven
├── mvnw / mvnw.cmd             # Maven Wrapper
├── .env.example                # Plantilla de variables del backend
└── README.md
```

---

## 🗂️ Entidades principales

### `Usuario`

Representa al usuario final de la app. Soporta registro local y vía Google.

| Campo      | Tipo                    | Descripción                                     |
|------------|-------------------------|-------------------------------------------------|
| `id`       | Long                    | Identificador autoincremental                   |
| `username` | String                  | Único, no nulo                                  |
| `password` | String                  | Hasheado con BCrypt (vacío para usuarios OAuth) |
| `email`    | String                  | Único, no nulo                                  |
| `role`     | Enum (`USER` / `ADMIN`) | Define permisos                                 |

### `Viaje`

Cada cálculo de viaje confirmado por un usuario se guarda como un registro.

| Campo                           | Tipo          | Descripción                   |
|---------------------------------|---------------|-------------------------------|
| `id`                            | Long          | Identificador autoincremental |
| `origen`                        | String        | Dirección textual del origen  |
| `destino`                       | String        | Dirección textual del destino |
| `distanciaEnMetros`             | Long          | Distancia devuelta por Google |
| `tiempoEstimadoMin`             | Integer       | Tiempo estimado en minutos    |
| `precioTaxi`                    | BigDecimal    | Precio calculado para Taxi    |
| `precioMinApp` / `precioMaxApp` | BigDecimal    | Rango estimado de Uber/Didi   |
| `fechaHora`                     | LocalDateTime | Timestamp del cálculo         |
| `usuario`                       | Usuario (FK)  | Dueño del viaje               |

### `Tarifa`

Permite parametrizar los precios sin tocar el código. Una fila por tipo de transporte.

| Campo                 | Tipo                          | Descripción                   |
|-----------------------|-------------------------------|-------------------------------|
| `id`                  | Long                          | Identificador autoincremental |
| `tipoTransporte`      | Enum (`TAXI`, `UBER`, `DIDI`) | Único                         |
| `precioBase`          | BigDecimal                    | Bajada de bandera / mínimo    |
| `precioPorKm`         | BigDecimal                    | Costo adicional por kilómetro |
| `ultimaActualizacion` | DateTime                      | Auto-actualizada por la DB    |

---

## ▶️ Cómo ejecutar el proyecto en local

> Si solo querés probar la app, no hace falta nada de esto — usá el [deploy](#-cómo-probar-la-app-en-1-minuto). Esta
> sección es para correrla localmente.

### 1. Requisitos previos

- **Java 21** (`java -version` debe mostrar 21.x).
- **Node.js 18+** y **npm**.
- Acceso a una base **MySQL** (local o la de AWS RDS provista por el equipo).
- **API key de Google Maps** con las APIs activas: *Distance Matrix*, *Places*, *Maps JavaScript*.
- **API key de OpenWeather** (gratuita).
- **OAuth Client de Google** (solo si querés probar "Continuar con Google").

### 2. Configuración del backend — `.env` en la raíz

Copiá `.env.example` a `.env` y completá:

```env
# Google Maps (backend usa Distance Matrix)
GOOGLE_MAPS_KEY=tu_google_maps_key

# OpenWeather
WEATHER_API_KEY=tu_openweather_key

# Base de datos (AWS RDS o MySQL local)
SPRING_DATASOURCE_URL=jdbc:mysql://tu-rds.amazonaws.com:3306/movilidadmdq
DB_USER=tu_usuario
DB_PASSWORD=tu_password

# JWT
JWT_SECRET=clave_base64_de_32_bytes_o_mas
JWT_EXPIRATION=86400000

# Google OAuth2 (opcional)
GOOGLE_OAUTH_CLIENT_ID=tu_client_id_google
GOOGLE_OAUTH_CLIENT_SECRET=tu_client_secret_google

# CORS
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Para generar un `JWT_SECRET` seguro:

```bash
openssl rand -base64 32
```

> 📌 El backend lee `.env` automáticamente desde `application.properties`. No es necesario exportar las variables a mano.

### 3. Configuración del frontend — `frontend/.env`

Copiá `frontend/.env.example` a `frontend/.env`:

```env
VITE_API_URL=http://localhost:8080
VITE_GOOGLE_MAPS_API_KEY=tu_google_maps_key_para_browser
```

> 📌 La key del frontend necesita **Maps JavaScript API** y **Places API**. En Google Cloud conviene restringirla por
> referer a `http://localhost:5173/*`.

### 4. Google OAuth2 (solo si vas a probar "Continuar con Google")

En **Google Cloud Console → APIs y servicios → Credenciales**, dentro del OAuth Client Web, agregar:

**Authorized redirect URI**

```text
http://localhost:8080/login/oauth2/code/google
```

**Authorized JavaScript origins**

```text
http://localhost:5173
http://localhost:8080
```

> ⚠️ Los origins no llevan path. Solo protocolo, host y puerto.

Después del login, el flujo vuelve al frontend en:

```text
http://localhost:5173/oauth2/redirect?token=...
```

### 5. Base de datos

Si arrancás con una DB vacía, ejecutá el script:

```bash
mysql -h <host> -u <user> -p < schema.sql
```

Esto crea las tablas `usuarios`, `tarifas` y `viajes`, e inserta las tarifas iniciales de Mar del Plata.

### 6. Levantar el backend

```bash
./mvnw spring-boot:run
```

Queda escuchando en `http://localhost:8080`.

### 7. Levantar el frontend

```bash
cd frontend
npm install
npm run dev
```

Queda en `http://localhost:5173`. Abrilo en el navegador.

### Alternativa con Docker (opcional)

```bash
docker-compose up --build
```

---

## 🔐 Autenticación y autorización

La API usa **JWT (JSON Web Tokens)** para autenticar las requests, con soporte adicional de **OAuth2 con Google**.

### Flujo clásico (email + contraseña)

1. El usuario se registra con `POST /usuarios/registro` o inicia sesión con `POST /usuarios/login`.
2. La respuesta incluye un campo `token` con el JWT.
3. Para todos los endpoints protegidos, el frontend envía:
   ```http
   Authorization: Bearer <token>
   ```
4. El backend valida el token con un filtro de Spring Security antes de ejecutar el controller.

### Flujo Google OAuth2

1. El usuario hace clic en "Continuar con Google" en el frontend.
2. El frontend redirige al backend (`/oauth2/authorization/google`).
3. Google autentica al usuario y vuelve al backend.
4. El backend genera un JWT propio y redirige al frontend a:
   ```text
   http://localhost:5173/oauth2/redirect?token=<jwt>
   ```
5. El frontend guarda el token y lo usa igual que el flujo clásico.

### Roles

| Rol     | Permisos                                                          |
|---------|-------------------------------------------------------------------|
| `USER`  | Calcular viajes, ver/modificar su propio perfil y su historial    |
| `ADMIN` | Todo lo anterior + actualizar tarifas (`PUT /admin/tarifas/taxi`) |

---

## 🔌 Endpoints

Todos los endpoints están documentados (con schemas y ejemplos) en **Swagger UI**.

| Método   | Endpoint                             | Auth      | Descripción                                |
|----------|--------------------------------------|-----------|--------------------------------------------|
| `POST`   | `/usuarios/registro`                 | ❌         | Crear usuario y devolver JWT               |
| `POST`   | `/usuarios/login`                    | ❌         | Login clásico y devolver JWT               |
| `GET`    | `/usuarios/me`                       | ✅         | Datos del usuario autenticado              |
| `PUT`    | `/usuarios/{id}`                     | ✅         | Actualizar perfil propio                   |
| `GET`    | `/usuarios/{id}/historial`           | ✅         | Historial de viajes del usuario            |
| `DELETE` | `/usuarios/{id}/historial/{viajeId}` | ✅         | Borrar un viaje del historial              |
| `GET`    | `/usuarios/{id}/viaje-frecuente`     | ✅         | Viaje más repetido del usuario             |
| `POST`   | `/viajes/calcular`                   | ✅         | Calcular viaje y guardarlo en el historial |
| `PUT`    | `/admin/tarifas/taxi`                | ✅ (ADMIN) | Actualizar la tarifa oficial de taxi       |

Endpoints protegidos requieren:

```http
Authorization: Bearer <tu_token>
```

---

## 📨 Ejemplos de requests y responses

### 1. Registrar usuario

```bash
curl -X POST http://localhost:8080/usuarios/registro \
  -H "Content-Type: application/json" \
  -d '{
    "username": "morena",
    "password": "Ramona00.2",
    "email": "morena@example.com"
  }'
```

**Response 200**:

```json
{
  "id": 3,
  "username": "morena",
  "email": "morena@example.com",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtb3JlbmEiLCJpYXQiOjE3MTcwMDAwMDB9...",
  "role": "USER"
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{ "username": "morena", "password": "Ramona00.2" }'
```

**Response 200**: misma forma que el registro (incluye `token`).

### 3. Calcular un viaje

```bash
curl -X POST http://localhost:8080/viajes/calcular \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "origen": "Luro y Mitre, Mar del Plata",
    "destino": "Juan B. Justo 1500, Mar del Plata",
    "origenLat": -38.0023,
    "origenLng": -57.5575,
    "destinoLat": -38.0214,
    "destinoLng": -57.5810
  }'
```

**Response 200**:

```json
[
  {
    "tipo": "TAXI",
    "precioMin": 7125.50,
    "precioMax": 7125.50,
    "tiempoMinutos": 15,
    "url": "https://www.taxis-mardelplata.com/"
  },
  {
    "tipo": "UBER",
    "precioMin": 6050.00,
    "precioMax": 8500.00,
    "tiempoMinutos": 15,
    "url": "https://m.uber.com/..."
  },
  {
    "tipo": "DIDI",
    "precioMin": 5800.00,
    "precioMax": 8100.00,
    "tiempoMinutos": 15,
    "url": "https://global.didiglobal.com/..."
  }
]
```

### 4. Ver historial

```bash
curl http://localhost:8080/usuarios/3/historial \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Response 200**:

```json
[
  {
    "id": 12,
    "origen": "Luro y Mitre, Mar del Plata",
    "destino": "Juan B. Justo 1500, Mar del Plata",
    "distanciaEnMetros": 5200,
    "tiempoEstimadoMin": 15,
    "precioTaxi": 7125.50,
    "precioMinApp": 6050.00,
    "precioMaxApp": 8500.00,
    "fechaHora": "2026-06-03T18:42:11"
  }
]
```

---

## 📘 Documentación navegable (Swagger UI)

**Producción (EC2)**:

- **Swagger UI** → https://movilidadmdq.ddns.net/swagger-ui.html
- **OpenAPI JSON** → https://movilidadmdq.ddns.net/api-docs

**Local**:

- **Swagger UI** → http://localhost:8080/swagger-ui.html
- **OpenAPI JSON** → http://localhost:8080/api-docs

Desde Swagger UI podés:

- Ver todos los endpoints con sus schemas.
- Probarlos directamente desde el navegador (botón **Try it out**).
- Autorizarte con el JWT (botón **Authorize** → pegar `Bearer <token>`).

---

## 🛠️ Verificar que todo compile

**Backend**:

```bash
./mvnw test
```

**Frontend**:

```bash
cd frontend && npm run build
```

---

## 🧯 Problemas comunes

| Problema              | Qué revisar                                                                          |
|-----------------------|--------------------------------------------------------------------------------------|
| No encuentra Java     | Instalar **Java 21** y verificar con `java -version`                                 |
| Error 401 al calcular | Falta iniciar sesión o el token venció                                               |
| Google Maps no carga  | `VITE_GOOGLE_MAPS_API_KEY` en `frontend/.env`                                        |
| Distance Matrix falla | `GOOGLE_MAPS_KEY` en `.env` raíz y APIs habilitadas en Google Cloud                  |
| OAuth2 falla          | Redirect URI y JavaScript origins en Google Cloud                                    |
| No conecta a AWS      | `SPRING_DATASOURCE_URL`, `DB_USER`, `DB_PASSWORD` y reglas del Security Group de RDS |
| CORS bloqueado        | Revisar `APP_CORS_ALLOWED_ORIGINS` en `.env` raíz                                    |

---

## 🧠 Notas para el equipo

- 🔒 No commitear `.env` ni claves reales.
- ✅ `frontend/` es la app vigente; ignorar cualquier carpeta legada.
- 🚀 En producción, restringir CORS y API keys al dominio real.
- 🧪 Antes de cada entrega, correr `./mvnw test` y `npm run build`.
