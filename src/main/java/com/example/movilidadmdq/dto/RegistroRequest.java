package com.example.movilidadmdq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistroRequest(
        @Schema(description = "Nombre de usuario", example = "ciroschot23")
        @NotBlank String username,

        @Schema(description = "Contraseña del usuario", example = "surfbody2333")
        @NotBlank String password,

        @Schema(description ="Email del usuario", example = "ciroschot@gmail.com")
        @NotBlank @Email String email
) {}
