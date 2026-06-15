package com.example.movilidadmdq.exception;

/**
 * Se lanza cuando la tarifa del taxi existe pero le faltan valores (campos en
 * NULL), así que no se puede calcular un precio.
 * <p>
 * Es un problema de configuración de datos, no del cliente: el
 * {@code GlobalExceptionHandler} la traduce a un HTTP 503 con un mensaje claro,
 * en vez de un 500 críptico por NullPointer.
 */
public class TarifaIncompletaException extends RuntimeException
{
    public TarifaIncompletaException(String mensaje)
    {
        super(mensaje);
    }
}
