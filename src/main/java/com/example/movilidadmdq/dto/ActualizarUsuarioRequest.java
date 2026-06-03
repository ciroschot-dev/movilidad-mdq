package com.example.movilidadmdq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ActualizarUsuarioRequest(
        @Schema(description = "Nombre de usuario", example = "morehidalggo")
        @NotBlank String username,

        @Schema(description = "Email del usuario", example = "morehidalggo@gmail.com")
        @NotBlank @Email String email,

        @Schema(description = "Contraseña del usuario", example = "morena123")
        String password
) {}
