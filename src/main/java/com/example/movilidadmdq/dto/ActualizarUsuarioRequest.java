package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos que manda un usuario ya registrado para modificar su perfil.
 * <p>
 * Al ser un record es inmutable, así los datos no se alteran en el camino del
 * controlador al service.
 */
public record ActualizarUsuarioRequest(
        @NotBlank(message = "El username es obligatorio")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        // Password opcional: si viene null o vacia, no se actualiza.
        // No le ponemos @NotBlank porque queremos permitir "no cambiarla".
        String password
) {}
