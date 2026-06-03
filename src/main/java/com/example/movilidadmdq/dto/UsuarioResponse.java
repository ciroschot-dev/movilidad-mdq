package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

public record UsuarioResponse(
        @Schema(description = "ID del usuario", example = "42")
        Long id,

        @Schema(description = "Nombre de usuario", example = "ciroschot23")
        String username,

        @Schema(description = "Email del usuario", example = "ciroschot@gmail.com")
        String email,

        @Schema(description = "Rol del usuario", example = "USER")
        Role role
) {}
