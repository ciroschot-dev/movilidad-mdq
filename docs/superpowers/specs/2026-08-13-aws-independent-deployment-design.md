# Deploy independiente de MovilidadMDQ en AWS

## Objetivo

Separar completamente el entorno de Ciro y Tiago del grupo anterior, manteniendo la aplicación disponible sin suspensión automática y reutilizando temporalmente el frontend actual de Vercel.

El nuevo entorno será considerado terminado cuando el frontend `movilidad-mdq.vercel.app` funcione de punta a punta contra un backend, una base de datos y credenciales administrados por Ciro.

## Arquitectura

```text
Vercel
  -> HTTPS
DuckDNS + Elastic IP
  -> Caddy en Docker
  -> Spring Boot en Docker :8080
  -> RDS MySQL 8.4 privado
```

- Región AWS: `us-east-1`.
- Hostname principal: `movilidad-mdq-backend.duckdns.org`.
- Fallback si no estuviera disponible: `movilidad-mdq-backend-cs.duckdns.org`.
- EC2: Ubuntu 24.04, `t3.micro`, 16 GB gp3 y 2 GB de swap.
- RDS: MySQL 8.4 LTS, `db.t3.micro`, Single-AZ, 20 GB, cifrado, backups por 7 días y sin acceso público.
- El Security Group de RDS permitirá `3306` solamente desde el Security Group del EC2.
- Caddy expondrá `80/443`; Spring Boot no expondrá `8080` a Internet.
- El deploy automático continuará usando SSH con clave. SSH no aceptará password ni root y Fail2ban limitará intentos abusivos.

## Repositorio y despliegue

Docker Compose administrará dos servicios: `app` y `caddy`. Los certificados y datos internos de Caddy vivirán en volúmenes persistentes. El archivo `.env` de producción permanecerá únicamente en el EC2 con permisos restrictivos.

GitHub Actions tendrá ejecución por push a `main` y manual mediante `workflow_dispatch`. El workflow actualizará el checkout con avance lineal, construirá con `docker compose`, comprobará la API a través del dominio HTTPS nuevo y mostrará logs cuando falle.

Los únicos secretos del workflow serán:

- `EC2_HOST`: Elastic IP nueva.
- `EC2_SSH_KEY`: clave privada nueva.

`BACKEND_URL` será una variable no secreta del repositorio.

Durante esta migración se permitirá integrar el cambio sin aprobación externa. Cuando producción quede validada, `main` requerirá un pull request con una aprobación.

## Configuración de aplicación

El backend conservará los endpoints y contratos REST actuales. No se migrarán datos; Hibernate creará el esquema sobre una base vacía y el bootstrap cargará las tarifas iniciales.

Producción usará:

```env
APP_CORS_ALLOWED_ORIGINS=https://movilidad-mdq.vercel.app,http://localhost:5173
APP_OAUTH2_REDIRECT_URI=https://movilidad-mdq.vercel.app/oauth2/redirect
```

Vercel usará:

```env
VITE_API_URL=https://movilidad-mdq-backend.duckdns.org
VITE_OAUTH_BASE_URL=https://movilidad-mdq-backend.duckdns.org
VITE_GOOGLE_MAPS_API_KEY=<clave-restringida>
```

Google OAuth autorizará exactamente:

```text
https://movilidad-mdq-backend.duckdns.org/login/oauth2/code/google
```

Ciro conservará sus credenciales de Google durante una transición coordinada y las rotará cuando el otro grupo tenga credenciales propias. Se creará una clave nueva de OpenWeather para este entorno.

## Separación de equipos

Antes de rotar credenciales o quitar permisos, el otro grupo deberá hacer fork, apuntar su EC2 al fork, crear su propio Vercel y configurar sus secrets.

Después de esa confirmación se quitará acceso de escritura a Anibal, Morena y Franco. Ciro y `tiagofueyovuillermoz-beep` conservarán acceso.

## Seguridad, costos y operación

- Activar MFA y verificar créditos antes de crear recursos.
- Crear alertas de presupuesto y revisar la continuidad antes de vencer los seis meses.
- Mantener RDS privado y no guardar credenciales en Git o GitHub Actions.
- Usar Elastic IP para estabilidad, aceptando que AWS factura las IPv4 públicas contra los créditos.
- No apagar automáticamente el EC2: la API debe responder siempre durante el período de uso.

## Validación y corte

Antes de cambiar Vercel:

1. Ejecutar tests Maven, lint y build del frontend.
2. Validar Docker Compose y el arranque contra RDS.
3. Confirmar HTTPS, Swagger, registro, JWT, Google OAuth, Maps, clima, cálculo, historial, eliminación y administración.
4. Confirmar que `8080` y RDS no sean públicos.
5. Reiniciar EC2 y comprobar persistencia y arranque automático.
6. Ejecutar manualmente GitHub Actions y verificar su healthcheck.

Solo entonces se reapuntará el proyecto actual de Vercel. Si el corte falla, se restaurarán las variables anteriores de Vercel mientras el backend viejo siga disponible.

## Fuera de alcance

- Rediseñar el frontend.
- Cambiar contratos REST o autenticación.
- Migrar datos del RDS anterior.
- Introducir Terraform, ECS, load balancers o AWS Secrets Manager.
