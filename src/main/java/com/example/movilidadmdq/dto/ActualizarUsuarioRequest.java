package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
 /*

        Este DTO se utiliza cuando un usuario ya registrado desea modificar sus datos
        personales.
        Al ser un 'record', el objeto es inmutable, lo que garantiza que los datos no se
        alteren
        durante el viaje desde el controlador hasta el servicio.*/



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
