package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DireccionFavoritaResponse(
        @Schema(description = "Nombre o dirección del lugar", example = "Plaza Mitre")
        String direccion,

        @Schema(description = "ID de Google Places", example = "ChIJ...")
        String placeId,

        @Schema(description = "Latitud", example = "-38.0055")
        Double lat,

        @Schema(description = "Longitud", example = "-57.5426")
        Double lng
) {}
