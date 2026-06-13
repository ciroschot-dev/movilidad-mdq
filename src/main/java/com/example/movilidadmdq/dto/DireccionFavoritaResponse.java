package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
        Este DTO de tipo 'record' se utiliza para enviar la información de una
        ubicación guardada por el usuario desde la base de datos hacia el frontend.

        SU FUNCIÓN:
        Permite que la sección de "Favoritos" en el Frontend (React) pueda
        listar las direcciones con sus nombres personalizados y re-utilizarlas
        rápidamente para nuevos cálculos sin que el usuario tenga que escribir todo de nuevo.
*/


public record DireccionFavoritaResponse(
        @Schema(description = "ID del favorito", example = "1")
        Long id,

        @Schema(description = "Nombre personalizado del lugar", example = "Casa")
        String nombre,

        @Schema(description = "Dirección completa del lugar", example = "Av. Colón 2500, Mar del Plata")
        String direccion,

        @Schema(description = "ID de Google Places", example = "ChIJ...")
        String placeId,

        @Schema(description = "Latitud", example = "-38.0055")
        Double lat,

        @Schema(description = "Longitud", example = "-57.5426")
        Double lng
) {}
