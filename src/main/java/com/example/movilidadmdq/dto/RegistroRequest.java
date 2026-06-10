package com.example.movilidadmdq.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegistroRequest(
        @Schema(description = "Nombre de usuario", example = "ciroschot23")
        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 30, message = "El username debe tener entre 3 y 30 caracteres")
        String username,

        @Schema(description = "Contraseña del usuario", example = "surfbody2333")
        @NotBlank(message = "La password es obligatoria")
        @Size(min = 6, message = "La password debe tener al menos 6 caracteres")
        String password,

        @Schema(description ="Email del usuario", example = "ciroschot@gmail.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email
) {}
