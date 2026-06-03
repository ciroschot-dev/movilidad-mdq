package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ViajeFrecuenteResponse(
        @Schema(description = "Origen del viaje frecuente", example = "Plaza Mitre")
        String origen,

        @Schema(description = "Destino del viaje frecuente", example = "Estadio José María Minella")
        String destino,

        @Schema(description = "Cantidad de veces que el usuario realizó este viaje", example = "5")
        Long cantidad
) {
}
