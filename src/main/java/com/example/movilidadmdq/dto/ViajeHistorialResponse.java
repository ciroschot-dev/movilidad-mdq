package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ViajeHistorialResponse(
        @Schema(description = "ID del viaje", example = "123")
        Long id,

        @Schema(description = "Origen del viaje", example = "Plaza Mitre")
        String origen,

        @Schema(description = "Destino del viaje", example = "Estadio José María Minella")
        String destino,

        @Schema(description = "Distancia del viaje en metros", example = "8200")
        Long distanciaEnMetros,

        @Schema(description = "Tiempo estimado del viaje en minutos", example = "18")
        Integer tiempoEstimadoMin,

        @Schema(description = "Precio estimado en taxi", example = "4800.00")
        BigDecimal precioTaxi,

        @Schema(description = "Precio estimado en Uber", example = "4200.00")
        BigDecimal precioUber,

        @Schema(description = "Precio estimado en Didi", example = "4100.00")
        BigDecimal precioDidi,

        @Schema(description = "Tipo de transporte elegido", example = "UBER")
        String tipoElegido,

        @Schema(description = "Fecha y hora en que se calculó el viaje", example = "2026-06-03T14:30:00")
        LocalDateTime fechaHora,

        @Schema(description = "Indica si el viaje está marcado como favorito", example = "true")
        boolean favorito,

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
