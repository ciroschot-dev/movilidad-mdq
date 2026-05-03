# Registro de Progreso - MovilidadMDQ

## Contexto del Proyecto
Aplicación para comparar precios de transporte (Taxi, Uber, Didi) en Mar del Plata.
- **Backend:** Java 25 (Spring Boot 4.x)
- **Frontend:** React 19 (TypeScript, Vite, Tailwind CSS)
- **Infraestructura:** Docker & Docker Compose

## Preferencias del Usuario
- **Idioma:** Castellano (Español)

## 📝 Resumen de la Sesión (2 de Mayo, 2026)
- **Conexión AWS RDS:** ¡LOGRADA! El backend ya se conecta a la base de datos MySQL en AWS.
- **Factores Dinámicos:** Verificados y activos en el backend.
- **Frontend Autocomplete:** Implementado con carga asíncrona moderna en la versión demo.
- **Versión Demo (Vanilla JS):** Funcionando con `env.js` para compatibilidad local y conexión al backend.

## 🔧 Configuración Actual
- **Base de Datos:** AWS RDS MySQL activa y vinculada al backend.
- **Google Maps Ready:** Backend y Frontend configurados para usar Google Maps API.
- **Factores Dinámicos ACTIVADOS:** `ViajeService.java` ya utiliza clima real y factores de horario/demanda.
- **Backend:** Refactorizado y con test de conexión inicial a la DB.

## 🚀 Próximos Pasos (Pendientes)
1. **Guardado de Historial:** Implementar el endpoint para guardar viajes cuando el usuario haga clic en "Elegir" desde el frontend.
2. **Persistencia de Usuarios:** Vincular el login/registro con la tabla de usuarios en AWS.
3. **Mejoras de UI:** Refinar los estilos de las sugerencias del autocompletado en la demo.

---
*Nota: Este archivo es la fuente de verdad para el contexto del Agente Gemini CLI en este proyecto.*
