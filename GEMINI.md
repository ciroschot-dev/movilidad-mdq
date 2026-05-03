# Registro de Progreso - MovilidadMDQ

## Contexto del Proyecto
Aplicación para comparar precios de transporte (Taxi, Uber, Didi) en Mar del Plata.
- **Backend:** Java 25 (Spring Boot 4.x)
- **Frontend:** React 19 (Producción) / HTML-JS Vanilla (Demo Académica)
- **Infraestructura:** Docker (Contenedores) & AWS RDS (Base de Datos en la Nube)

## 📝 Resumen de la Sesión (2 de Mayo, 2026)
- **Conexión AWS RDS:** Backend conectado exitosamente a MySQL en AWS. Verificado mediante logs automáticos al inicio.
- **Persistencia Automática:** Se implementó el guardado automático de cada consulta de viaje en la base de datos de AWS.
- **Frontend Demo (Vanilla):** Se creó una versión completa en `frontend-demo/` que incluye:
  - **Login:** Autenticación contra el backend y persistencia de sesión en `localStorage`.
  - **Historial:** Pantalla que recupera y muestra los viajes realizados desde la base de datos de AWS.
  - **Perfil (CRUD):** Pantalla para editar datos del usuario (Nombre, Email, Password) con persistencia real.
  - **Navegación:** Sistema de vistas dinámicas sin recarga de página.
- **Docker:** Configurado `docker-compose` y `Dockerfile` (Multi-stage con Java 25) para despliegue aislado.
- **Fixes:** Corregida firma de métodos en `ViajeServiceDebugTest.java` para permitir la compilación exitosa del JAR.

## 🔧 Configuración Actual
- **Base de Datos:** AWS RDS activa. Endpoint configurado en `.env`.
- **CORS:** Habilitado en el backend (`@CrossOrigin("*")`) para permitir conexiones desde la demo local.
- **Variables de Entorno Front:** Manejadas mediante `frontend-demo/env.js` para compatibilidad con protocolo `file://` (doble clic).

## 🚀 Próximos Pasos (Pendientes)
1. **Seguridad de API:** Restringir el origen de CORS para producción.
2. **Google Maps Android:** Si se pasa a Android, configurar la API Key con restricciones de paquete.
3. **Refinar Historial:** Añadir filtros por rango de fechas en la UI del historial.
4. **Validaciones:** Fortalecer las validaciones de datos en el cambio de perfil (longitud de password, formato de email).

---
*Nota: Este archivo es la fuente de verdad para el contexto del Agente Gemini CLI en este proyecto.*
