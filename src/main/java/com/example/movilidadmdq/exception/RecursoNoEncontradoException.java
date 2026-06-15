package com.example.movilidadmdq.exception;

/**
 * Se lanza cuando se busca algo (un usuario, un viaje, una tarifa) y no existe.
 * <p>
 * Tener una excepción propia le permite al {@code GlobalExceptionHandler}
 * traducirla a un HTTP 404 (Not Found) sin confundirla con otros errores.
 */
public class RecursoNoEncontradoException extends RuntimeException
{
    public RecursoNoEncontradoException(String mensaje)
    {
        super(mensaje);
    }
}
