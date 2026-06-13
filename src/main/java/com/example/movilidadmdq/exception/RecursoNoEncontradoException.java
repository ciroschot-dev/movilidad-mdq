package com.example.movilidadmdq.exception;

/* 
   CLASE: RecursoNoEncontradoException
   
   Esta es una excepción personalizada que se lanza cuando el sistema busca algo 
   en la base de datos (un Usuario, un Viaje, una Tarifa) y no lo encuentra.
   
   ¿POR QUÉ CREARLA?:
   Hereda de 'RuntimeException' para que Spring pueda capturarla automáticamente. 
   Al tener una clase específica, el 'GlobalExceptionHandler' puede saber exactamente 
   que debe responder un código HTTP 404 (Not Found).
*/
public class RecursoNoEncontradoException extends RuntimeException
{
    /* 
       Constructor que recibe el mensaje de error personalizado.
       Ej: "No se encontró el viaje con ID 5".
    */
    public RecursoNoEncontradoException(String mensaje)
    {
        super(mensaje);
    }
}
