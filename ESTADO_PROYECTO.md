# Estado del Proyecto - MovilidadMDQ

## 📝 Resumen de la Sesión (28 de Abril, 2026)
Se preparó la infraestructura para la integración con Google Maps y se mejoró la robustez del cálculo de viajes.

## 🔧 Configuración Actual
- **Google Maps Ready:** Backend preparado con `GoogleMapsService`. Usa la API Key desde variables de entorno o `application.properties`.
- **Fallback de Seguridad:** Si la API de Google falla, el sistema sigue funcionando con valores simulados (5km / 15min).
- **Backend:** Spring Boot 3.2.5 (JDK 17).

## 🚀 Pendientes
1. **Factores Dinámicos:** Activar la lógica de clima, horario y demanda en `ViajeService.java` (el código ya está preparado en comentarios).
2. **Frontend Autocomplete:** Implementar la búsqueda de direcciones con Google Places API en React.
3. **API Key:** Configurar una API Key real para probar los cálculos exactos.
4. **Persistencia:** Reactivar MySQL cuando se requiera guardar historial de viajes.

---
*Nota: Este archivo sirve para sincronizar el contexto entre diferentes sesiones del agente CLI.*
