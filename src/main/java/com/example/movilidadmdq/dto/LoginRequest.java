package com.example.movilidadmdq.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**

       Esta clase es un DTO (Data Transfer Object) de tipo 'record'.
       Su función es capturar las credenciales (usuario y contraseña) que el usuario
       ingresa en el formulario de inicio de sesión del frontend.

       SEGURIDAD:
       Al ser un 'record', los datos son inmutables. Solo se utilizan para ser
       validados por Spring Security en el proceso de autenticación.
*/


public record LoginRequest(
        @Schema(description = "Nombre de usuario para ingresar", example = "Morehidalggo")
        @NotBlank(message = "El username es obligatorio")
        String username,

        @Schema(description = "Contraseña para ingresar", example = "Ramona00.2")
        @NotBlank(message = "La password es obligatoria")
        String password) {}
