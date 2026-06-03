package com.example.movilidadmdq.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CalculoViajeRequest(
        @Schema(description = "Nombre del origen", example = "Pinamar")
        @NotBlank String origen,

        @Schema(description = "Nombre del destino", example = "Mar del Plata")
        @NotBlank String destino,

        @Schema(description = "Origen en coordenadas latitud", example = "-38.0")
        Double origenLat,

        @Schema(description = "Origen en longitud", example = "-32.1200")
        Double origenLng,

        @Schema(description = "Destino en coordenadas latitud", example = "-38.0")
        Double destinoLat,

        @Schema(description = "Destino en longitud", example = "32.1200")
        Double destinoLng
)
{
}