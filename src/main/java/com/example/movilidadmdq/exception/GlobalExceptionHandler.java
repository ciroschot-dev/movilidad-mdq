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

/**
 * Traduce cualquier excepción de la app a una respuesta HTTP uniforme.
 * <p>
 * Toda excepción que tiren los services o controllers "sube" hasta acá. Esto
 * evita repetir try-catch en cada método, hace que todos los errores salgan con
 * la misma forma ({@link ApiError}) y deja a los services preocupados solo por
 * lanzar la excepción, sin decidir el código HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{

    /** Recurso buscado que no existe → 404 Not Found. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    /** Acción sobre datos ajenos u otra regla de negocio violada → 403 Forbidden. */
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ApiError> manejarNoPermitido(OperacionNoPermitidaException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.FORBIDDEN, ex.getMessage(), req, null);
    }

    /** Alta de un email o username ya existente → 409 Conflict. */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarDuplicado(RecursoDuplicadoException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    /** Usuario o contraseña incorrectos (lo tira Spring Security) → 401 Unauthorized. */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> manejarCredencialesInvalidas(BadCredentialsException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", req, null);
    }

    /**
     * Fallos de las validaciones {@code @Valid}/{@code @NotBlank} → 400 Bad Request.
     * <p>
     * Devuelve además la lista de qué campos fallaron y por qué.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest req)
    {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatearError)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos invalidos", req, detalles);
    }

    /** El cuerpo del pedido no es un JSON válido → 400 Bad Request. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarBodyIlegible(HttpMessageNotReadableException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es un JSON valido", req, null);
    }

    /** Falta configuración de tarifas en la base → 503 Service Unavailable. */
    @ExceptionHandler(TarifaIncompletaException.class)
    public ResponseEntity<ApiError> manejarTarifaIncompleta(TarifaIncompletaException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req, null);
    }

    /**
     * Atrapa-todo para cualquier error inesperado (bugs) → 500 Internal Server Error.
     * <p>
     * Evita que la app se caiga y oculta el detalle interno por seguridad.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerico(Exception ex, HttpServletRequest req)
    {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req, null);
    }

    // Arma el ApiError estandarizado que devuelven todos los manejadores de arriba.
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

    // Convierte un error de validación en texto "campo: mensaje".
    private String formatearError(FieldError error)
    {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
