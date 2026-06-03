package com.example.movilidadmdq.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Nombre de usuario para ingresar", example = "Morehidalggo")
        @NotBlank String username,

        @Schema(description = "Contraseña para ingresar", example = "Ramona00.2")
        @NotBlank String password) {}
