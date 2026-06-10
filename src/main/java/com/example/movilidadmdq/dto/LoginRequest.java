package com.example.movilidadmdq.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Nombre de usuario para ingresar", example = "Morehidalggo")
        @NotBlank(message = "El username es obligatorio")
        String username,

        @Schema(description = "Contraseña para ingresar", example = "Ramona00.2")
        @NotBlank(message = "La password es obligatoria")
        String password) {}
