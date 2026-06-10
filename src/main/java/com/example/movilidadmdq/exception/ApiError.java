package com.example.movilidadmdq.exception;

import java.time.LocalDateTime;
import java.util.List;

// DTO uniforme de respuesta de error. Cualquier error que devuelva la API
// viaja con esta forma, asi el frontend (o quien consuma) siempre sabe
// que leer.
//
// - timestamp: cuando ocurrio el error (server time).
// - status:    codigo HTTP (404, 409, etc.).
// - error:     nombre corto del estado HTTP ("Not Found", "Conflict", ...).
// - message:   mensaje legible para humanos.
// - path:      la URL que se estaba pidiendo cuando se rompio.
// - errores:   detalle por campo cuando viene de una validacion @Valid.
//              Es null en los errores que no son de validacion.
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> errores
)
{
}
