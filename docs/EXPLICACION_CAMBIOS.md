# 🧩 Explicacion De Cambios

Este documento resume que hicimos en MovilidadMDQ, por que lo hicimos y como quedo funcionando la app. La idea es que
cualquier companero pueda leerlo y entender el flujo completo sin tener que revisar todo el codigo.

## 🚦 Resumen En 1 Minuto

Antes la app tenia partes separadas y seguridad incompleta. Ahora quedo asi:

```text
Frontend React -> Backend Spring Boot -> AWS RDS
      |                  |
      |                  +-> Google Maps / OpenWeather
      |
      +-> Login normal o Google OAuth2
```

La app actual usa:

- ✅ `frontend/` como frontend principal.
- ✅ Spring Security con JWT.
- ✅ Login normal.
- ✅ Login con Google OAuth2.
- ✅ Historial real por usuario.
- ✅ AWS RDS como base de datos.

`frontend-demo/` queda como demo vieja y referencia historica.

## 🧱 Que Cambiamos

| Area      | Cambio                                                                  |
|-----------|-------------------------------------------------------------------------|
| Seguridad | Se agrego Spring Security real                                          |
| Passwords | Se guardan con BCrypt                                                   |
| Login     | Login clasico devuelve JWT                                              |
| Google    | OAuth2 crea/busca usuario y devuelve JWT                                |
| Frontend  | React ahora maneja login, registro, logout, Google, calculo e historial |
| Historial | Cada usuario ve solo sus viajes                                         |
| Calculo   | El usuario se toma del JWT, no de un `usuarioId` del frontend           |
| DTOs      | Se agregaron respuestas seguras para no exponer entidades completas     |
| CORS      | Se centralizo en `SecurityConfig`                                       |
| Arranque  | Se elimino el chequeo automatico que leia viajes reales al iniciar      |
| Docs      | Se actualizaron README, AGENTS, GEMINI y examples de env                |

## 🖥️ Frontend Actual Vs Demo Vieja

### ✅ Frontend actual

```text
frontend/
```

Este es el que se usa para correr la app.

Incluye:

- Login.
- Registro.
- Boton de Google.
- Calculo de viaje.
- Historial.
- Manejo de JWT.

### 🕰️ Frontend viejo

```text
frontend-demo/
```

No se usa como app principal. Sirvio como referencia porque tenia ideas de historial/perfil, pero no esta adaptado al
flujo nuevo con JWT.

## 🔐 Como Funciona El Login Normal

Flujo:

```text
Usuario -> Frontend -> POST /usuarios/login -> Backend -> JWT -> Frontend
```

Pasos:

1. El usuario se registra o inicia sesion.
2. El frontend llama a uno de estos endpoints:

```text
POST /usuarios/registro
POST /usuarios/login
```

3. El backend responde con:

```json
{
  "id": 1,
  "username": "demo",
  "email": "demo@test.com",
  "token": "jwt..."
}
```

4. El frontend guarda la sesion en `localStorage`:

```text
movilidadmdq.auth.v1
```

5. Desde ese momento, cada request protegida manda:

```text
Authorization: Bearer <token>
```

## 🔵 Como Funciona Google OAuth2

Flujo visual:

```text
Frontend
  -> /oauth2/authorization/google
  -> Google Login
  -> /login/oauth2/code/google
  -> Backend genera JWT
  -> /oauth2/redirect?token=...
  -> Frontend consulta /usuarios/me
```

Pasos:

1. El usuario toca `Continuar con Google`.
2. El frontend redirige a:

```text
http://localhost:8080/oauth2/authorization/google
```

3. Google autentica al usuario.
4. Google vuelve al backend:

```text
http://localhost:8080/login/oauth2/code/google
```

5. El backend crea o busca el usuario por email.
6. El backend genera JWT.
7. El backend vuelve al frontend:

```text
http://localhost:5173/oauth2/redirect?token=...
```

8. El frontend toma el token y llama a:

```text
GET /usuarios/me
```

9. El frontend arma la sesion y ya se puede usar la app.

## ⚙️ Configuracion Necesaria En Google Cloud

En el OAuth Client de Google Cloud hay que configurar(ya lo hizo Ciro):

### Authorized redirect URI

```text
http://localhost:8080/login/oauth2/code/google
```

### Authorized JavaScript origins

```text
http://localhost:5173
http://localhost:8080
```

⚠️ Importante: los origins no llevan path. Son solo protocolo, host y puerto.

## 🚕 Como Funciona Calcular Viaje

Antes el frontend enviaba algo asi:

```json
{
  "origen": "...",
  "destino": "...",
  "usuarioId": 1
}
```

Eso no era seguro porque alguien podia mandar el id de otro usuario.

Ahora el frontend envia solo:

```json
{
  "origen": "Colon 2090, Mar del Plata",
  "destino": "Colon 3132, Mar del Plata"
}
```

Y manda el token:

```text
Authorization: Bearer <token>
```

El backend toma el usuario desde el JWT y guarda el viaje para ese usuario.

Endpoint:

```text
POST /viajes/calcular
```

## 🧾 Como Funciona El Historial

El frontend tiene una pestaña llamada `Historial`.

Cuando el usuario entra, llama a:

```text
GET /usuarios/{id}/historial
```

con el JWT.

El backend valida:

```text
id de la URL == id del usuario autenticado
```

Si coincide, devuelve los viajes. Si no coincide, devuelve `403`.

Ademas devuelve un DTO seguro:

```text
ViajeHistorialResponse
```

Asi no se expone la entidad `Usuario` ni el password.

## 🔑 Variables De Entorno

Hay dos `.env` diferentes.

### Backend

Archivo:

```text
.env
```

Variables:

```env
GOOGLE_MAPS_KEY=...
WEATHER_API_KEY=...
SPRING_DATASOURCE_URL=...
DB_USER=...
DB_PASSWORD=...
JWT_SECRET=...
JWT_EXPIRATION=86400000
GOOGLE_OAUTH_CLIENT_ID=...
GOOGLE_OAUTH_CLIENT_SECRET=...
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
```

### Frontend

Archivo:

```text
frontend/.env
```

Variables:

```env
VITE_API_URL=http://localhost:8080
VITE_GOOGLE_MAPS_API_KEY=...
```

## 🧪 Como Probar Todo

### 1. Levantar backend

```bash
./mvnw spring-boot:run
```

### 2. Levantar frontend

```bash
cd frontend && npm run dev
```

### 3. Abrir navegador

```text
http://localhost:5173
```

### 4. Checklist manual

- ✅ Registro normal.
- ✅ Login normal.
- ✅ Login con Google.
- ✅ Calcular viaje.
- ✅ Ver historial.
- ✅ Cerrar sesion.
- ✅ Volver a entrar y confirmar que historial sigue estando.

## 🧰 Como Verificar Que No Se Rompio Nada

Backend:

```bash
./mvnw test
```

Frontend:

```bash
cd frontend && npm run build
```

## 📁 Archivos Importantes

| Archivo                                 | Para que sirve                                      |
|-----------------------------------------|-----------------------------------------------------|
| `SecurityConfig.java`                   | Reglas de seguridad, OAuth2 y CORS                  |
| `JwtAuthFilter.java`                    | Lee y valida el JWT                                 |
| `JwtService.java`                       | Genera y valida tokens                              |
| `CustomOAuth2UserService.java`          | Crea o busca usuarios de Google                     |
| `OAuth2SuccessHandler.java`             | Genera JWT despues del login Google                 |
| `UsuarioController.java`                | Login, registro, usuario actual, historial y perfil |
| `ViajeController.java`                  | Calculo protegido de viajes                         |
| `frontend/src/App.tsx`                  | Flujo principal del frontend actual                 |
| `frontend/src/components/InputForm.tsx` | Formulario origen/destino con Places                |

## 🧹 Pendientes Recomendados

- 🗑️ Decidir si se borra `frontend-demo/`.
- 👤 Agregar pantalla de perfil al frontend React.
- 🧪 Agregar tests de seguridad para `401` y `403`.
- ⚠️ Mostrar aviso si Google Maps falla y se usa distancia fallback.
- 🔒 En produccion, restringir CORS y API keys al dominio real.

## ✅ Estado Actual

- Login/registro funciona.
- OAuth2 con Google funciona si Google Cloud esta bien configurado.
- El frontend actual es `frontend/`.
- El calculo de viajes usa JWT y no confia en `usuarioId` del frontend.
- Historial esta integrado en React y filtrado por usuario.
- `./mvnw test` pasa.
- `npm run build` en `frontend/` pasa.
