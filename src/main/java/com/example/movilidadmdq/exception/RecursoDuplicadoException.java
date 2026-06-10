package com.example.movilidadmdq.exception;

// Se lanza al intentar crear un recurso con un identificador unico que ya
// existe (por ejemplo, registrar un username o email ya tomado).
// El GlobalExceptionHandler la traduce a HTTP 409 (Conflict).
public class RecursoDuplicadoException extends RuntimeException
{
    public RecursoDuplicadoException(String mensaje)
    {
        super(mensaje);
    }
}
