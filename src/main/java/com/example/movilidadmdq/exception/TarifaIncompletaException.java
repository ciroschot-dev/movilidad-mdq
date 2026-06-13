package com.example.movilidadmdq.exception;

// Se lanza cuando la tarifa del taxi existe en la DB pero le faltan valores
// (campos en NULL), así que no se puede calcular un precio. Es un problema de
// configuración de datos, no del cliente: el GlobalExceptionHandler la traduce
// a HTTP 503 con un mensaje claro, en vez de un 500 críptico por NullPointer.
public class TarifaIncompletaException extends RuntimeException
{
    public TarifaIncompletaException(String mensaje)
    {
        super(mensaje);
    }
}
