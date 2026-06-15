package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.ViajeFrecuenteResponse;
import com.example.movilidadmdq.dto.ActualizarUsuarioRequest;
import com.example.movilidadmdq.dto.AuthResponse;
import com.example.movilidadmdq.dto.LoginRequest;
import com.example.movilidadmdq.dto.RegistroRequest;
import com.example.movilidadmdq.dto.UsuarioResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.service.UsuarioService;
import com.example.movilidadmdq.service.HistorialViajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Operaciones de la cuenta del usuario: registro, login, perfil e historial.
 * <p>
 * Toma la identidad logueada con {@code @AuthenticationPrincipal Usuario} y
 * delega en los services, que validan que cada usuario acceda solo a sus propios
 * datos (que el usuario A no vea ni borre los viajes del usuario B).
 */
@Tag(name = "Usuarios", description = "Registro, login, perfil e historial de viajes del usuario.")
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController
{
    private final UsuarioService usuarioService;
    private final HistorialViajeService historialViajeService;

    @Operation(summary = "Ingresar", description = "Se ingresan credenciales para iniciar sesion, devuelve token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso, devuelve el token"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o faltantes"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request)
    {
        log.info("""

        🔐 INICIANDO FLUJO LOGIN:
        -> 1. UsuarioController.login: Recibe credenciales.
        -> 2. UsuarioService.login: Valida credenciales y busca usuario.
        -> 3. JwtService.generateToken: Crea token JWT firmado.
        -> 4. Retorno: Se envía AuthResponse al frontend.
        """);

        // Sin try/catch: si las credenciales son invalidas, Spring Security
        // tira BadCredentialsException y el GlobalExceptionHandler la traduce
        // a un 401. Lo unico que hace este metodo es orquestar.
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @Operation(summary = "Registrarse", description = "Se ingresan credenciales para registrarse, devuelve token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro exitoso, devuelve el token"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Username o email ya registrados")
    })

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request)
    {
        // Sin try/catch: las validaciones de @Valid tiran MethodArgumentNotValid
        // (400) y los duplicados tiran RecursoDuplicadoException (409). Ambos
        // los traduce el GlobalExceptionHandler.
        return ResponseEntity.ok(usuarioService.registrar(request));
    }

    @Operation(summary = "Obtener usuario actual", description = "Devuelve los datos del usuario autenticado ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devuelve datos del usuario"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido")
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioActual(@AuthenticationPrincipal Usuario usuario)
    {
        return ResponseEntity.ok(usuarioService.toResponse(usuario));
    }

    @Operation(summary = "Obtener el historial de un usuario segun ID", description = "Devuelve una lista como historial del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos del usuario"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido"),
            @ApiResponse(responseCode = "403", description = "No tenés permiso para ver el historial de otro usuario")
    })

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<ViajeHistorialResponse>> obtenerHistorial(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario)
    {
        return ResponseEntity.ok(historialViajeService.obtenerHistorial(usuario, id));
    }

    @Operation(summary = "Borrar el historial de un usuario segun ID", description = "Borra historial del usuario de la base de datos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Borrado historial del usuario con exito"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido"),
            @ApiResponse(responseCode = "403", description = "No tenés permiso para borrar el historial de otro usuario")
    })

    @DeleteMapping("/{id}/historial/{viajeId}")
    public ResponseEntity<Void> borrarViaje(
            @PathVariable Long id,
            @PathVariable Long viajeId,
            @AuthenticationPrincipal Usuario usuario
    )
    {
        historialViajeService.borrarViaje(usuario, id, viajeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener viaje frecuente de un usuario segun ID", description = "Obtener viaje frecuente del usuario de la base de datos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Viaje frecuente del usuario hallado con exito"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido"),
            @ApiResponse(responseCode = "403", description = "No tenés permiso para ver los viajes de otro usuario")
    })

    @GetMapping("/{id}/viaje-frecuente")
    public ResponseEntity<ViajeFrecuenteResponse> obtenerViajeFrecuente(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario
    )
    {
        return historialViajeService.obtenerViajeFrecuente(usuario, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Actualizar el perfil del usuario", description = "Se usa el ID del usuario para actualizar sus datos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se actualizo el perfil del usuario con exito"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permiso para actualizar perfil")
    })

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest datosNuevos, @AuthenticationPrincipal Usuario usuario)
    {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(usuario, id, datosNuevos));
    }

    @Operation(summary = "Buscar usuario por username o email", description = "Devuelve los datos de un usuario buscado. Solo accesible por administradores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> buscarUsuario(@RequestParam String query)
    {
        return ResponseEntity.ok(usuarioService.buscar(query));
    }

    @Operation(summary = "Eliminar cuenta de usuario", description = "Elimina permanentemente la cuenta del usuario. Los usuarios pueden eliminar su propia cuenta, y los administradores pueden eliminar cualquier cuenta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuenta eliminada con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permiso para eliminar esta cuenta")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario)
    {
        usuarioService.eliminarCuenta(usuario, id);
        return ResponseEntity.noContent().build();
    }
}
