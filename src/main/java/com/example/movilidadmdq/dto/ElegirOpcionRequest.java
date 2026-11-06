package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.TipoTransporte;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Petición para registrar la opción de transporte que el usuario terminó eligiendo (RF10).
 */
public record ElegirOpcionRequest(
        @Schema(description = "Tipo de transporte elegido (TAXI, UBER o DIDI)", example = "UBER")
        @NotNull(message = "El tipo de transporte no puede ser nulo")
        TipoTransporte tipo
) {
}
