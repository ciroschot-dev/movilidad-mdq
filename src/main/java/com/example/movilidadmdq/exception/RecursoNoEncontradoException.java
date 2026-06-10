package com.example.movilidadmdq.exception;

// Se lanza cuando se busca un recurso por id, username o email y la DB
// no devuelve nada. El GlobalExceptionHandler la traduce a HTTP 404.
public class RecursoNoEncontradoException extends RuntimeException
{
    public RecursoNoEncontradoException(String mensaje)
    {
        super(mensaje);
    }
}
