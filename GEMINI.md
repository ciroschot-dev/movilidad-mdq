# Registro de Progreso - MovilidadMDQ

## Contexto del Proyecto
Aplicación para comparar precios de transporte (Taxi, Uber, Didi) en Mar del Plata.
- **Backend:** Java (Spring Boot)
- **Frontend:** React (TypeScript, Vite, Tailwind CSS)

## Preferencias del Usuario
- **Idioma:** Castellano (Español)

## Estado Actual
- **MVP Funcional:** El sistema ya permite ingresar origen/destino y muestra comparativas.
- **Lógica de Precios:** Taxi usa tarifas reales (diurna/nocturna). Uber y Didi usan actualmente multiplicadores simples sobre el precio del taxi.
- **Código Pendiente:** Existe lógica comentada en `ViajeService.java` para mejorar la estimación de precios usando factores dinámicos (Clima, Horario, Demanda).

## Próximos Pasos (Hoja de Ruta)
1.  **Refactorizar Precios:** Activar e integrar la lógica de factores dinámicos para Uber y Didi.
2.  **Integración con Google Maps:** Sustituir la distancia simulada (5km) por el cálculo de distancia real entre direcciones.
3.  **Autocompletado:** Implementar búsqueda de direcciones en el frontend.
4.  **Mejoras de UI:** Refinar la visualización de resultados.
