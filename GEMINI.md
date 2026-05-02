# Registro de Progreso - MovilidadMDQ

## Contexto del Proyecto
Aplicación para comparar precios de transporte (Taxi, Uber, Didi) en Mar del Plata.
- **Backend:** Java 25 (Spring Boot 4.x)
- **Frontend:** React 19 (TypeScript, Vite, Tailwind CSS)
- **Infraestructura:** Docker & Docker Compose

## Preferencias del Usuario
- **Idioma:** Castellano (Español)

## 📝 Resumen de la Sesión (2 de Mayo, 2026)
- **Factores Dinámicos:** Verificados y activos en el backend.
- **Frontend Autocomplete:** ¡IMPLEMENTADO! Se integró `use-places-autocomplete` y Google Maps API en el frontend para búsqueda de direcciones en Mar del Plata.
- **Refactorización:** Se eliminaron valores harcodeados en `ViajeService.java` (tarifas, teléfono) moviéndolos a constantes y configuraciones.

## 🔧 Configuración Actual
- **Google Maps Ready:** Backend y Frontend configurados para usar Google Maps API.
- **Factores Dinámicos ACTIVADOS:** `ViajeService.java` ya utiliza clima real y factores de horario/demanda.
- **Backend:** Refactorizado para mayor mantenibilidad.
- **Docker:** Configurado para desplegar el backend (Eclipse Temurin 25).

## 🚀 Próximos Pasos (Pendientes)
1. **API Key Frontend:** Asegurar que `VITE_GOOGLE_MAPS_API_KEY` esté configurada en el entorno para que el autocompletado funcione.
2. **Persistencia:** Reactivar MySQL para guardar historial de viajes y preferencias de usuario.
3. **Mejoras de UI:** Refinar los estilos de las sugerencias del autocompletado para que coincidan con la estética del proyecto.
4. **Refinar Demanda:** Buscar una forma más real de calcular el factor de demanda (actualmente aleatorio).

---
*Nota: Este archivo es la fuente de verdad para el contexto del Agente Gemini CLI en este proyecto.*
