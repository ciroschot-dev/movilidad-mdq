# Guía de pruebas con Swagger UI

Guía paso a paso para probar la API y demostrar el manejo centralizado
de errores HTTP. Pensada para la defensa del TP UTN.

## 0. URLs

- **Local:** `http://localhost:8080/swagger-ui.html`
- **Producción:** `https://movilidadmdq.ddns.net/swagger-ui/index.html`

> ⚠️ Si vas a probar contra prod, asegurate de haber pusheado los
> cambios y que el servidor ya esté con la versión nueva.

---

## 1. Levantar el backend (solo si pruebo local)

```bash
cd "/Users/ciro-schot/Projects/Spring Boot/movilidadMDQ"
./mvnw spring-boot:run
```

Esperar a ver `Started MovilidadMdqApplication`. Después abrir
`http://localhost:8080/swagger-ui.html` en el navegador.

---

## 2. Tour rápido por Swagger UI

Al abrir la página vas a ver:

- **Tags** agrupando endpoints: *Usuarios*, *Viajes*, *Tarifas - Admin*.
- Cada endpoint tiene: método (GET/POST/PUT/DELETE), ruta, descripción y
  los **Responses** documentados (200, 400, 401, 403, 404, 409).
- Botón **"Authorize"** arriba a la derecha (candado): ahí se carga el JWT
  para que todos los endpoints autenticados lo usen sin reescribirlo.
- En cada endpoint hay un botón **"Try it out"** que habilita la edición
  del body y un **"Execute"** que dispara el request.

**Qué decirle al profe acá:**
> "Esta documentación se genera automáticamente desde las anotaciones
> `@Operation`, `@ApiResponse` y `@Schema` que están en los controllers
> y DTOs. Es OpenAPI 3, estándar de industria."

### Detalle a destacar: ejemplos de error reales

Si expandís cualquier endpoint y mirás los responses **400/401/403/404/409**,
vas a ver que cada uno muestra un **Example Value** coherente con su
código (el 401 dice `"status": 401`, el 404 dice `"status": 404`, etc.) y
con el shape de `ApiError`. Eso lo hace un único bean en `OpenApiConfig`
que centraliza la documentación de errores — mismo patrón que el
`GlobalExceptionHandler` aplicado a la doc.

**Qué decirle al profe:**
> "Igual que el handler global centraliza la traducción a HTTP, hay un
> `OpenApiCustomizer` que centraliza la documentación de los errores en
> Swagger. Un único lugar mantiene el formato visible para el cliente
> y el formato visible para quien lee la documentación."

---

## 3. Caso 200 — Registrar un usuario nuevo

**Endpoint:** `POST /usuarios/registro`

1. Click en el endpoint → **Try it out**.
2. En el body pegar:
   ```json
   {
     "username": "ciroDemo",
     "password": "demo123",
     "email": "ciro.demo@example.com"
   }
   ```
3. **Execute**.

**Resultado esperado:** `200 OK` con un JSON que incluye `id`, `username`,
`email`, **`token`** y `role`.

4. **Copiar el `token`** del response (sin las comillas).

**Qué mostrar al profe:**
> "El registro devuelve un JWT firmado por el backend. Ese token lo voy
> a usar para autenticar el resto de los endpoints."

---

## 4. Cargar el token con "Authorize"

1. Click en el botón **Authorize** (candado, arriba a la derecha).
2. Pegar el token en el campo. **No** hace falta escribir "Bearer", Swagger
   lo agrega solo. Si tu configuración pide el formato completo, pegá:
   `Bearer <token-que-copiaste>`.
3. Click **Authorize** → **Close**.

A partir de acá, todos los endpoints autenticados van a llevar el header
`Authorization: Bearer <token>` automáticamente.

---

## 5. Caso 400 — Validaciones de input (Bean Validation)

**Endpoint:** `POST /usuarios/registro`

1. **Try it out** y pegar este body inválido:
   ```json
   {
     "username": "x",
     "password": "123",
     "email": "no-es-email"
   }
   ```
2. **Execute**.

**Resultado esperado:** `400 Bad Request` con este shape:
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Datos invalidos",
  "path": "/usuarios/registro",
  "errores": [
    "username: El username debe tener entre 3 y 30 caracteres",
    "email: El email no tiene un formato valido",
    "password: La password debe tener al menos 6 caracteres"
  ]
}
```

**Qué mostrar al profe:**
> "Las validaciones son declarativas en el DTO con `@NotBlank`, `@Email`,
> `@Size`. El handler global captura la `MethodArgumentNotValidException`
> y arma una lista de errores por campo. El frontend puede mostrar cada
> mensaje al lado del input correspondiente."

---

## 6. Caso 409 — Recurso duplicado

**Endpoint:** `POST /usuarios/registro`

Volver a registrar el usuario del paso 3 con el **mismo username**:

```json
{
  "username": "ciroDemo",
  "password": "demo123",
  "email": "otro.email@example.com"
}
```

**Resultado esperado:** `409 Conflict`:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "El username ya esta registrado",
  "path": "/usuarios/registro",
  "errores": null
}
```

**Qué mostrar al profe:**
> "El service tira una `RecursoDuplicadoException`, que el handler
> traduce a 409. Antes de refactorizar esto devolvía un 400 difuso —
> ahora el código HTTP es semánticamente correcto: 400 es para input
> mal formado, 409 es para conflicto con el estado del recurso."

---

## 7. Caso 401 — Credenciales inválidas

**Endpoint:** `POST /usuarios/login`

```json
{
  "username": "ciroDemo",
  "password": "password-incorrecta"
}
```

**Resultado esperado:** `401 Unauthorized`:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciales invalidas",
  "path": "/usuarios/login",
  "errores": null
}
```

**Qué mostrar al profe:**
> "Spring Security tira `BadCredentialsException`. El handler la captura
> y devuelve 401 con un body uniforme. Antes este endpoint devolvía body
> vacío; ahora el cliente tiene una respuesta consistente con todos los
> otros errores de la API."

---

## 8. Caso 404 — Recurso no encontrado

Necesitamos estar logueados. Ya tenemos token cargado en *Authorize*
del paso 4.

**Endpoint:** `PUT /viajes/{viajeId}/favorito`

1. **Try it out**.
2. En `viajeId` poner: `999999`.
3. **Execute**.

**Resultado esperado:** `404 Not Found`:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Viaje no encontrado",
  "path": "/viajes/999999/favorito",
  "errores": null
}
```

**Qué mostrar al profe:**
> "El service busca el viaje con `findById` y, si no existe, tira
> `RecursoNoEncontradoException`. El handler la traduce a 404. El service
> no sabe nada de HTTP."

---

## 9. Caso 403 — Operación no permitida (recurso de otro usuario)

Este es el más interesante de defender. Necesitamos:
- Usuario A (el que está logueado) querer modificar un viaje de Usuario B.

### 9.1. Generar un viaje del usuario actual

**Endpoint:** `POST /viajes/calcular`

```json
{
  "origen": "Plaza San Martin Mar del Plata",
  "destino": "Estacion de tren Mar del Plata"
}
```

Esto guarda un viaje en el historial del usuario logueado. **Copiá el
`id` del viaje** que aparece en el response (o anotalo del próximo paso).

### 9.2. Ver mis viajes para confirmar el id

**Endpoint:** `GET /usuarios/me` → copiar el `id` del usuario.

**Endpoint:** `GET /usuarios/{id}/historial` → ver el `id` del viaje que
acabás de crear.

### 9.3. Registrar un segundo usuario y loguear con él

Vas a Authorize → **Logout** → registrás otro usuario:

```json
{
  "username": "otroUser",
  "password": "demo123",
  "email": "otro@example.com"
}
```

Copiás su token y lo cargás en **Authorize**.

### 9.4. Intentar modificar el viaje del otro

**Endpoint:** `PUT /viajes/{viajeId}/favorito` con el `viajeId` que
guardaste en el paso 9.2 (el viaje pertenece a `ciroDemo`, ahora estás
logueado como `otroUser`).

**Resultado esperado:** `403 Forbidden`:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "No tienes permiso para modificar este viaje",
  "path": "/viajes/{id}/favorito",
  "errores": null
}
```

**Qué mostrar al profe (este es el argumento estrella):**
> "Acá hay una decisión de diseño importante: el recurso existe pero no
> es del usuario autenticado. ¿Por qué 403 y no 404?
>
> - **404** sería "mentir": estaría ocultando que el viaje existe, pero
>   también estaría rompiendo la semántica HTTP — el viaje SÍ está en
>   la DB.
> - **403** es lo correcto: "sé quién sos (el JWT es válido), pero esto
>   no te pertenece".
>
> El service tira `OperacionNoPermitidaException`. Antes el código tiraba
> un `RuntimeException` genérico que caía en el handler 500. Ahora la
> respuesta es semánticamente clara."

---

## 10. Cierre — qué decir al final de la demo

> "Lo que mostré aplica Single Responsibility Principle en tres niveles:
>
> 1. **Los services** describen el problema en términos del dominio
>    (`RecursoNoEncontradoException`, `OperacionNoPermitidaException`,
>    `RecursoDuplicadoException`). No saben nada de HTTP.
> 2. **El `@RestControllerAdvice`** centraliza la traducción a respuestas
>    HTTP. Si mañana queremos cambiar un 409 por 422, lo hacemos en un
>    solo lugar.
> 3. **Los controllers** solo orquestan: reciben el request, delegan al
>    service, devuelven la respuesta. No tienen `try/catch` de tipos
>    genéricos.
>
> Las validaciones de input son declarativas con Bean Validation y
> también las captura el handler global. Todas las respuestas de error
> usan el mismo shape (`ApiError`), lo que le da consistencia al
> contrato de la API."

---

## Tabla resumen de casos a demostrar

| Paso | Caso | Endpoint | HTTP | Excepción |
|---|---|---|---|---|
| 3 | Registro exitoso | `POST /usuarios/registro` | 200 | — |
| 5 | Input inválido | `POST /usuarios/registro` | **400** | `MethodArgumentNotValidException` |
| 6 | Usuario duplicado | `POST /usuarios/registro` | **409** | `RecursoDuplicadoException` |
| 7 | Login con password mal | `POST /usuarios/login` | **401** | `BadCredentialsException` |
| 8 | Viaje inexistente | `PUT /viajes/999999/favorito` | **404** | `RecursoNoEncontradoException` |
| 9 | Viaje de otro usuario | `PUT /viajes/{id}/favorito` | **403** | `OperacionNoPermitidaException` |
