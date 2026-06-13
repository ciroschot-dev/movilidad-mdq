package com.example.movilidadmdq.exception;

/* 
   CLASE: RecursoDuplicadoException
   
   Excepción lanzada cuando se intenta crear un registro que viola una restricción 
   de unicidad en la base de datos. 
   
   USO PRINCIPAL: 
   En el 'UsuarioService', cuando un nuevo usuario intenta registrarse con un 
   'username' o 'email' que ya pertenecen a otra cuenta.
*/
public class RecursoDuplicadoException extends RuntimeException
{
    /* 
       Constructor para el mensaje de error. 
       El 'GlobalExceptionHandler' traducirá esta excepción a un código HTTP 409 (Conflict).
    */
    public RecursoDuplicadoException(String mensaje)
    {
        super(mensaje);
    }
}
