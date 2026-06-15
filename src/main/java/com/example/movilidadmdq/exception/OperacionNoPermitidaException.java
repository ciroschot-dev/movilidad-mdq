package com.example.movilidadmdq.exception;

/**
 * Se lanza cuando el usuario intenta una acción sobre datos que no son suyos.
 * <p>
 * Por ejemplo, marcar como favorito un viaje de otro usuario: técnicamente es
 * una operación válida, pero rompe las reglas de propiedad. El
 * {@code GlobalExceptionHandler} la traduce a un HTTP 403 (Forbidden).
 */
public class OperacionNoPermitidaException extends RuntimeException
{
    public OperacionNoPermitidaException(String mensaje)
    {
        super(mensaje);
    }
}
