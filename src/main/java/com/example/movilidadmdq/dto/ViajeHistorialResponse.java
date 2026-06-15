package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * La "foto" de un viaje guardado, tal como quedó al momento de consultarlo.
 * <p>
 * Se usa para mostrarle al usuario su historial y para la auditoría del
 * administrador. Guarda los precios de aquel momento, el transporte elegido y
 * los datos geográficos necesarios para volver a dibujar la ruta en el mapa.
 */
public record ViajeHistorialResponse(
        /** ID del registro en la tabla de viajes. */
        @Schema(description = "ID del viaje", example = "123")
        Long id,

        /** Origen y destino tal como se consultaron. */
        @Schema(description = "Origen del viaje", example = "Plaza Mitre")
        String origen,

        @Schema(description = "Destino del viaje", example = "Estadio José María Minella")
        String destino,

        // === Datos del trayecto (distancia y tiempo que dio Google Maps) ===
        @Schema(description = "Distancia del viaje en metros", example = "8200")
        Long distanciaEnMetros,

        @Schema(description = "Tiempo estimado del viaje en minutos", example = "18")
        Integer tiempoEstimadoMin,

        // === Comparativa de precios al momento de la consulta ===
        @Schema(description = "Precio estimado en taxi", example = "4800.00")
        BigDecimal precioTaxi,

        @Schema(description = "Precio estimado en Uber", example = "4200.00")
        BigDecimal precioUber,

        @Schema(description = "Precio estimado en Didi", example = "4100.00")
        BigDecimal precioDidi,

        /** Transporte que el usuario eligió, si llegó a elegir uno. */
        @Schema(description = "Tipo de transporte elegido", example = "UBER")
        String tipoElegido,

        /** Fecha y hora de la consulta. Se usa para ordenar el historial. */
        @Schema(description = "Fecha y hora en que se calculó el viaje", example = "2026-06-03T14:30:00")
        LocalDateTime fechaHora,

        /** Si el usuario marcó este viaje como favorito. */
        @Schema(description = "Indica si el viaje está marcado como favorito", example = "true")
        boolean favorito,

        /** Quién hizo el viaje. Se usa sobre todo en el panel de admin. */
        @Schema(description = "Username del usuario que realizó el viaje", example = "juan.perez")
        String username,

        // === Datos geográficos (place id + coordenadas) para redibujar la ruta
        //     en el mapa sin que el usuario vuelva a buscar las direcciones ===
        @Schema(description = "Place ID de origen")
        String origenPlaceId,
        @Schema(description = "Latitud de origen")
        Double origenLat,
        @Schema(description = "Longitud de origen")
        Double origenLng,
        @Schema(description = "Place ID de destino")
        String destinoPlaceId,
        @Schema(description = "Latitud de destino")
        Double destinoLat,
        @Schema(description = "Longitud de destino")
        Double destinoLng
) {}
