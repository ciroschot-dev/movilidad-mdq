package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.Role;

public record UsuarioResponse(
        @Schema(description="Id del usuario", example = "33")
        Long id,

        @Schema(description = "nombre del usuario", example=" pepito123")
        String username,

        @Schema(description = "email del usuario", example = "pepito@gmail.com")
        String email,

        @Schema(description ="rol del usuario", example = "admin")
        Role role
) {}
