package com.example.movilidadmdq.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/* 
   CLASE: GlobalExceptionHandler
   
   Esta clase actúa como el "Controlador de Emergencias" de toda la aplicación.
   Cualquier excepción que ocurra en un Service o Controller "sube" hasta aquí.
   
   ¿POR QUÉ USARLO?:
   1. Centraliza la lógica de errores: No repetimos bloques try-catch en cada método.
   2. Consistencia: Todas las respuestas de error tienen la misma estructura JSON (ApiError).
   3. Desacoplamiento: Los servicios solo lanzan excepciones, no deciden el código HTTP.
*/
@RestControllerAdvice
public class GlobalExceptionHandler
{

    /* 
       MANEJADOR: Recurso No Encontrado (404)
       Se dispara cuando buscamos un ID que no existe.
    */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    /* 
       MANEJADOR: Operación No Permitida (403)
       Se dispara por violaciones de reglas de negocio (ej: borrar datos de otro usuario).
    */
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ApiError> manejarNoPermitido(OperacionNoPermitidaException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.FORBIDDEN, ex.getMessage(), req, null);
    }

    /* 
       MANEJADOR: Recurso Duplicado (409)
       Se dispara cuando se intenta registrar un email o username ya existente.
    */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarDuplicado(RecursoDuplicadoException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    /* 
       MANEJADOR: Credenciales Inválidas (401)
       Atrapa el error de Spring Security cuando el password o usuario son incorrectos.
    */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> manejarCredencialesInvalidas(BadCredentialsException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", req, null);
    }

    /* 
       MANEJADOR: Errores de Validación (400)
       Atrapa los fallos de las anotaciones @Valid, @NotBlank, etc. 
       Devuelve una lista de qué campos fallaron específicamente.
    */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest req)
    {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatearError)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos invalidos", req, detalles);
    }

    /* 
       MANEJADOR: JSON Malformado (400)
       Si el frontend manda un JSON roto o mal escrito.
    */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarBodyIlegible(HttpMessageNotReadableException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es un JSON valido", req, null);
    }

    /* 
       MANEJADOR: Tarifa Incompleta (503)
       Error de servidor cuando falta configuración vital en la base de datos de tarifas.
    */
    @ExceptionHandler(TarifaIncompletaException.class)
    public ResponseEntity<ApiError> manejarTarifaIncompleta(TarifaIncompletaException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req, null);
    }

    /* 
       MANEJADOR: Genérico (500)
       El "Atrapa-Todo" para cualquier error inesperado (bugs). 
       Evita que la aplicación se caiga y muestra un mensaje genérico por seguridad.
    */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerico(Exception ex, HttpServletRequest req)
    {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req, null);
    }

    /* 
       MÉTODO AUXILIAR: construir
       Crea el objeto ApiError estandarizado para todas las respuestas.
    */
    private ResponseEntity<ApiError> construir(HttpStatus status, String mensaje, HttpServletRequest req, List<String> errores)
    {
        ApiError body = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                req.getRequestURI(),
                errores
        );
        return ResponseEntity.status(status).body(body);
    }

    /* 
       MÉTODO AUXILIAR: formatearError
       Extrae el nombre del campo y el mensaje de error de validación.
    */
    private String formatearError(FieldError error)
    {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
