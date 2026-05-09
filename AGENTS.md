# AGENTS.md

Guia de contexto para cualquier agente IA que trabaje en MovilidadMDQ. Este archivo reemplaza al antiguo `GEMINI.md` como fuente general para modelos.

## Resumen Del Proyecto

MovilidadMDQ es una app academica para comparar opciones de transporte en Mar del Plata. Calcula alternativas de Taxi, Uber y Didi, muestra precios estimados y guarda historial de viajes por usuario en AWS RDS.

## Stack Actual

- Backend: Java 21, Spring Boot 4.0.6, Maven Wrapper.
- Seguridad: Spring Security, JWT propio con `io.jsonwebtoken`, BCrypt y OAuth2 Client con Google.
- Base de datos: MySQL en AWS RDS mediante Spring Data JPA.
- Frontend: React 19, TypeScript, Vite, Tailwind CSS.
- APIs externas: Google Maps Distance Matrix, Google Places Autocomplete y OpenWeather.
- Docker: backend con `Dockerfile` y `docker-compose.yml`.

## Estructura Relevante

- `src/main/java/com/example/movilidadmdq/security`: JWT, OAuth2, configuracion de seguridad.
- `src/main/java/com/example/movilidadmdq/config`: filtro JWT y beans compartidos como `PasswordConfig`.
- `src/main/java/com/example/movilidadmdq/controller`: controladores REST.
- `src/main/java/com/example/movilidadmdq/service`: logica de usuarios, viajes, Google Maps y clima.
- `src/main/java/com/example/movilidadmdq/model`: entidades JPA.
- `src/main/java/com/example/movilidadmdq/dto`: requests/responses.
- `frontend/src/App.tsx`: flujo principal de autenticacion y calculo.
- `frontend/src/components/InputForm.tsx`: formulario de origen/destino con Places Autocomplete.
- `frontend-demo/`: demo HTML/JS legado. No es el frontend principal; solo sirve como referencia historica.

## Flujo De Seguridad

Login clasico:

1. `POST /usuarios/registro` crea usuario con password BCrypt y rol `USER`.
2. `POST /usuarios/login` autentica con `AuthenticationManager`.
3. Backend devuelve `AuthResponse` con `id`, `username`, `email` y `token`.
4. Frontend guarda la sesion en `localStorage` bajo `movilidadmdq.auth.v1`.
5. Frontend envia `Authorization: Bearer <token>` en endpoints protegidos.

OAuth2 Google:

1. Frontend redirige a `/oauth2/authorization/google`.
2. Google vuelve a `/login/oauth2/code/google`.
3. `CustomOAuth2UserService` crea o recupera el usuario por email.
4. `OAuth2SuccessHandler` genera JWT y redirige a `http://localhost:5173/oauth2/redirect?token=...` por defecto.
5. Frontend toma el token, llama a `GET /usuarios/me`, arma la sesion y continua.

Google Cloud local:

- Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`.
- Authorized JavaScript origins: `http://localhost:5173` y `http://localhost:8080`.
- Los origins no llevan path; solo protocolo, host y puerto.

Autorizacion:

- Publicos: `/usuarios/login`, `/usuarios/registro`, `/oauth2/**`, `/login/**`, `/error`.
- Protegidos: cualquier otro endpoint.
- Admin: `/admin/**` requiere `ROLE_ADMIN`.

## Endpoints Principales

- `POST /usuarios/registro`: body `{ username, password, email }`, devuelve `AuthResponse`.
- `POST /usuarios/login`: body `{ username, password }`, devuelve `AuthResponse`.
- `GET /usuarios/me`: requiere Bearer JWT, devuelve `UsuarioResponse`.
- `POST /viajes/calcular`: requiere Bearer JWT, body `{ origen, destino }`; el usuario se toma del JWT.
- `GET /usuarios/{id}/historial`: requiere Bearer JWT, devuelve solo viajes del usuario autenticado como DTO de historial.
- `PUT /usuarios/{id}`: requiere Bearer JWT, actualiza perfil y encodea nueva password si viene.

## Variables De Entorno

Backend lee `.env` en la raiz via `spring.config.import=optional:file:.env[.properties]`.

Variables requeridas:

- `GOOGLE_MAPS_KEY`: API key backend para Google Maps Distance Matrix.
- `WEATHER_API_KEY`: API key de OpenWeather.
- `SPRING_DATASOURCE_URL`: JDBC de AWS RDS MySQL.
- `DB_USER`: usuario de base.
- `DB_PASSWORD`: password de base.
- `JWT_SECRET`: secreto base64 para HS256, generado por ejemplo con `openssl rand -base64 32`.
- `JWT_EXPIRATION`: expiracion en milisegundos.
- `GOOGLE_OAUTH_CLIENT_ID`: client ID OAuth2 Google.
- `GOOGLE_OAUTH_CLIENT_SECRET`: client secret OAuth2 Google.

Variable opcional:

- `app.oauth2.redirectUri`: URL frontend donde el backend redirige despues de OAuth2. Default: `http://localhost:5173/oauth2/redirect`.

Frontend lee `frontend/.env`:

- `VITE_API_URL`: URL del backend, normalmente `http://localhost:8080`.
- `VITE_GOOGLE_MAPS_API_KEY`: API key browser para Google Places Autocomplete.

## Comandos De Verificacion

Backend tests:

```bash
./mvnw test
```

Backend run:

```bash
./mvnw spring-boot:run
```

Frontend build:

```bash
cd frontend
npm run build
```

Frontend dev:

```bash
cd frontend
npm run dev
```

## Estado Verificado

Al momento de esta actualizacion:

- `./mvnw test` pasa.
- `npm run build` en `frontend/` pasa.
- Backend arranca con Java 21.
- Frontend usa JWT para `/viajes/calcular`; no debe enviar `usuarioId`.
- Frontend muestra historial desde `GET /usuarios/{id}/historial`.
- OAuth2 Google esta integrado con redirect a frontend.

## Convenciones Para Cambios

- No commitear `.env`, claves reales ni secretos.
- Mantener Java 21, no volver a documentar Java 25.
- Usar DTOs para nuevas respuestas/requests, no exponer entidades directamente si se agrega API nueva.
- Passwords siempre con `PasswordEncoder`/BCrypt.
- Endpoints protegidos deben asumir Bearer JWT salvo login/registro/OAuth2.
- Si se cambia el contrato backend, actualizar `frontend/src/App.tsx` y este documento.
- Si se cambia configuracion de entorno, actualizar `.env.example`, `frontend/.env.example` y `README.md`.
- Evitar refactors grandes sin necesidad; este proyecto todavia es academico y conviene mantener cambios pequenos y faciles de revisar.

## Riesgos Conocidos

- CORS se configura con `APP_CORS_ALLOWED_ORIGINS`; default `http://localhost:5173`.
- `frontend-demo/` puede quedar desactualizado; no tomarlo como fuente de verdad.
- `ViajeServiceDebugTest` usa keys dummy en test y puede loguear errores esperados de APIs externas, pero los tests pasan.
- Google Maps requiere configurar APIs correctas y restricciones adecuadas en Google Cloud.
