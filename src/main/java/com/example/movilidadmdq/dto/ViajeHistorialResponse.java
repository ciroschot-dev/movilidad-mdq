package com.example.movilidadmdq.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ViajeHistorialResponse(
        @Schema(description = "Id del usuario", example = "33")
        Long id,

        @Schema(description = "Origen del viaje",example = "colon 1736")
        String origen,

        @Schema(description = "Destino del viaje",example = "Tuyu 2024")
        String destino,

        @Schema(description = "Distancia en metros entre origen y destino", example ="5km")
        Long distanciaEnMetros,

        @Schema(description = "Tiempo estimado en llegar desde origen a destino", example = "10")
        Integer tiempoEstimadoMin,

        @Schema(description = "Precio del taxi del viaje", example = "5000")
        BigDecimal precioTaxi,

        @Schema(description = "Precio minimo del viaje", example = "2100")
        BigDecimal precioMinApp,

        @Schema(description = "Precio maximo del viaje", example = "7800")
        BigDecimal precioMaxApp,

        @Schema(description = "Hora que se pide el viaje", example "")
        LocalDateTime fechaHora
) {}
