package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos necesarios para calcular un viaje")

/**
        Este DTO es el encargado de transportar los datos desde el formulario de búsqueda
        del frontend hasta el motor de cálculo del backend.

        IMPORTANCIA:
        No solo envía texto (direcciones), sino que incluye metadatos geográficos (Place IDs,
        Latitud, Longitud) obtenidos previamente por el frontend a través de la API de
        Autocompletado de Google. Esto asegura que el cálculo de distancia sea exacto.
 */


public record CalculoViajeRequest(
        @Schema(description = "Direccion de origen", example = "Paseo Costanera Mar del Plata")
        @NotBlank(message = "El origen es obligatorio")
        String origen,

        @Schema(description = "Direccion de destino", example = "Grand Beach")
        @NotBlank(message = "El destino es obligatorio")
        String destino,

        @Schema(description = "Nombre corto del lugar de origen", example = "Paseo Costanera Mar del Plata")
        String origenAddressLine1,

        @Schema(description = "Direccion completa del lugar de origen", example = "Santiago del Estero 1202, Mar del Plata")
        String origenAddressLine2,

        @Schema(description = "Google Place ID del origen", example = "ChIJKwxUL7bdhJUR_yRC-2o5c_c")
        String origenPlaceId,

        @Schema(description = "Latitud del origen", example = "-37.99572510000001")
        Double origenLat,

        @Schema(description = "Longitud del origen", example = "-57.542423400000004")
        Double origenLng,

        @Schema(description = "Nombre corto del lugar de destino", example = "Grand Beach")
        String destinoAddressLine1,

        @Schema(description = "Direccion completa del lugar de destino", example = "Saavedra 65, Mar del Plata")
        String destinoAddressLine2,

        @Schema(description = "Google Place ID del destino", example = "ChIJD0zcgMvdhJURkZqCZ7ciEBY")
        String destinoPlaceId,

        @Schema(description = "Latitud del destino", example = "-38.0277922")
        Double destinoLat,

        @Schema(description = "Longitud del destino", example = "-57.532317199999994")
        Double destinoLng
) {
}