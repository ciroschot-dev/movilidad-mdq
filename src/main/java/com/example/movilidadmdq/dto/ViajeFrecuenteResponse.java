package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * El trayecto (origen-destino) que el usuario repite más seguido.
 * <p>
 * Le sirve al frontend para ofrecer un acceso directo o una sugerencia
 * personalizada, en vez de hacer que el usuario reescriba siempre el mismo viaje.
 */
public record ViajeFrecuenteResponse(
        /** Punto de partida más habitual. */
        @Schema(description = "Origen del viaje frecuente", example = "Plaza Mitre")
        String origen,

        /** Punto de llegada más habitual. */
        @Schema(description = "Destino del viaje frecuente", example = "Estadio José María Minella")
        String destino,

        /** Cuántas veces se repitió este par origen-destino: justifica por qué es "frecuente". */
        @Schema(description = "Cantidad de veces que el usuario realizó este viaje", example = "5")
        Long cantidad
) {
}
