package com.example.movilidadmdq.exception;

/**
 * Se lanza cuando se intenta crear algo que ya existe y debe ser único.
 * <p>
 * El caso típico está en el {@code UsuarioService}: alguien se registra con un
 * username o email que ya pertenece a otra cuenta. El
 * {@code GlobalExceptionHandler} la traduce a un HTTP 409 (Conflict).
 */
public class RecursoDuplicadoException extends RuntimeException
{
    public RecursoDuplicadoException(String mensaje)
    {
        super(mensaje);
    }
}
