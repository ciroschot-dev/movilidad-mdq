package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ActualizarUsuarioRequest(
        @Schema(description = "Nuevo nombre de usuario", example = "ciroschot23")
        @NotBlank String username,

        @Schema(description = "Nuevo email del usuario", example = "ciroschot@gmail.com")
        @NotBlank @Email String email,

        @Schema(description = "Nueva contraseña (opcional, si se omite no se modifica)", example = "nuevaPass123")
        String password
) {}
