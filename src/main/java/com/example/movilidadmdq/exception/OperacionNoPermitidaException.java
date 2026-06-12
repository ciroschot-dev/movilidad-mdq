package com.example.movilidadmdq.exception;

// Se lanza cuando un usuario autenticado intenta operar sobre un recurso
// que no le pertenece (por ejemplo, marcar como favorito el viaje de otro).
// El GlobalExceptionHandler la traduce a HTTP 403.
public class OperacionNoPermitidaException extends RuntimeException
{
    public OperacionNoPermitidaException(String mensaje)
    {
        super(mensaje);
    }
}
