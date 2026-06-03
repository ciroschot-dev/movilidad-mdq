package com.example.movilidadmdq.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public record ViajeFrecuenteResponse(
        @Schema(description = "Origen del viaje frecuente", example = "Colon 1736")
        String origen,

        @Schema(description = "Destino del viaje frecuente", example = "Saavedra 56")
        String destino,

        @Schema(description = "Cantidad de veces que se pidio el vijae", example = "3")
        Long cantidad
) {
}