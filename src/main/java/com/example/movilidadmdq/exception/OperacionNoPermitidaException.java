package com.example.movilidadmdq.exception;

/* 
   CLASE: OperacionNoPermitidaException
   
   Esta excepción se dispara cuando un usuario intenta realizar una acción 
   que, aunque sea válida técnicamente, viola las reglas de negocio o de 
   propiedad de datos del sistema.
   
   EJEMPLO: 
   Intentar marcar como "favorito" un viaje que le pertenece a otro usuario.
*/
public class OperacionNoPermitidaException extends RuntimeException
{
    /* 
       Constructor para el mensaje de error. 
       El 'GlobalExceptionHandler' traducirá esta excepción a un código HTTP 403 (Forbidden).
    */
    public OperacionNoPermitidaException(String mensaje)
    {
        super(mensaje);
    }
}
