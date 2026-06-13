package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
/*

        Esta clase es un DTO (Data Transfer Object) de tipo 'record'.
        Es la respuesta oficial del servidor tras un login o registro exitoso.

        SU FUNCIÓN:
        Contiene toda la información necesaria para que el Frontend (React)
        pueda configurar la sesión del usuario, guardar el token y saber
        qué permisos (rol) tiene para mostrar u ocultar botones.

*/



public record AuthResponse(
        @Schema(description = "ID del usuario", example = "42")
        Long id,

        @Schema(description = "Nombre de usuario", example = "ciroschot23")
        String username,

        @Schema(description = "Email del usuario", example = "ciroschot@gmail.com")
        String email,

        @Schema(description = "JWT a usar en el header Authorization: Bearer <token>",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjaXJvIn0.x9k")
        String token,

        @Schema(description = "Rol del usuario", example = "USER")
        Role role
) {}
