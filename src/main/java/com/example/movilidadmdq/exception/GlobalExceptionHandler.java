package com.example.movilidadmdq.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

// Toda excepcion que sube desde un @RestController pasa por aca.
// Centraliza la traduccion de excepciones a respuestas HTTP, asi los
// services solo se preocupan por describir el problema (que paso) y los
// controllers solo orquestan. El "como se devuelve" vive en un solo lugar.
@RestControllerAdvice
public class GlobalExceptionHandler
{

    // Recurso buscado por id/username/email que no existe -> 404.
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    // El usuario autenticado quiere tocar algo que no le pertenece -> 403.
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ApiError> manejarNoPermitido(OperacionNoPermitidaException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.FORBIDDEN, ex.getMessage(), req, null);
    }

    // Intento de crear algo con un identificador unico que ya existe -> 409.
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarDuplicado(RecursoDuplicadoException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    // Login con credenciales mal. Spring Security ya lanza esta excepcion;
    // la traducimos al 401 en formato ApiError para mantener consistencia.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> manejarCredencialesInvalidas(BadCredentialsException ex, HttpServletRequest req)
    {
        return construir(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", req, null);
    }

    // Validaciones declarativas (@Valid en el controller) que fallan.
    // Devolvemos 400 con la lista de campos invalidos para que el frontend
    // pueda mostrar el error junto al input correcto.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest req)
    {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatearError)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos invalidos", req, detalles);
    }

    // Red de seguridad: cualquier cosa que no matchee con los handlers
    // anteriores cae aca y se devuelve como 500 sin filtrar el stacktrace.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerico(Exception ex, HttpServletRequest req)
    {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req, null);
    }

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

    private String formatearError(FieldError error)
    {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
