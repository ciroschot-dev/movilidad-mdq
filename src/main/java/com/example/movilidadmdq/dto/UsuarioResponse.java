package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

/**

Esta clase es un DTO (Data Transfer Object) de tipo 'record'.
Representa la información de un usuario que el sistema considera segura para enviar
al exterior.

IMPORTANCIA ESTRATÉGICA:
Se utiliza para desacoplar la Entidad de la Base de Datos ('Usuario') de la respuesta
de la API.
De esta manera, nos aseguramos de que campos sensibles como la contraseña NUNCA
viajen hacia el frontend, cumpliendo con las normas básicas de ciberseguridad.

*/

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
