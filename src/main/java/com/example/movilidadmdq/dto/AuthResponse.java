package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.Role;

public record AuthResponse(
        @Schema(descrption = "ID del usuario", example = "9999")
        Long id,

        @Schema(description = "Nombre de usuario", example = "morehidalggo")
        String username,

        @Schema(description = "Email del usuario", example = "morehidalggo@gmail.com")
        String email,

        @Schema(description = "Token de autorizacion", example = "TK872K0183")
        String token,

        @Schema(description = "Rol del usuario", example = "Admin")
        Role role) {}
