package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ActualizarDireccionFavoritaRequest(
        @Schema(description = "Nuevo nombre personalizado", example = "Trabajo")
        @NotBlank(message = "El nombre no puede estar vacío")
        String nombre
) {}
