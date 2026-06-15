package com.example.movilidadmdq.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Forma única de toda respuesta de error de la API.
 * <p>
 * Cualquier error viaja con esta estructura, así el frontend (o quien consuma)
 * siempre sabe qué leer:
 * <ul>
 *   <li>{@code timestamp}: cuándo ocurrió el error (hora del servidor).</li>
 *   <li>{@code status}: código HTTP (404, 409, etc.).</li>
 *   <li>{@code error}: nombre corto del estado HTTP ("Not Found", "Conflict").</li>
 *   <li>{@code message}: mensaje legible para humanos.</li>
 *   <li>{@code path}: la URL que se estaba pidiendo cuando se rompió.</li>
 *   <li>{@code errores}: detalle por campo cuando viene de una validación
 *       {@code @Valid}. Es null en los errores que no son de validación.</li>
 * </ul>
 */
public record ApiError(
        @Schema(description = "Momento del error en el servidor", example = "2026-06-10T12:34:56.789")
        LocalDateTime timestamp,

        @Schema(description = "Codigo HTTP", example = "400")
        int status,

        @Schema(description = "Nombre corto del estado HTTP", example = "Bad Request")
        String error,

        @Schema(description = "Mensaje legible para humanos", example = "Datos invalidos")
        String message,

        @Schema(description = "URL que se estaba pidiendo cuando ocurrio el error", example = "/usuarios/registro")
        String path,

        @Schema(
                description = "Detalle por campo cuando viene de una validacion @Valid. Null en errores que no son de validacion.",
                example = "[\"email: El email no tiene un formato valido\"]"
        )
        List<String> errores
)
{
}
