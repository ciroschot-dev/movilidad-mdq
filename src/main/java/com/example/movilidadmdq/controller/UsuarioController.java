package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.ViajeFrecuenteResponse;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.movilidadmdq.dto.ActualizarUsuarioRequest;
import com.example.movilidadmdq.dto.AuthResponse;
import com.example.movilidadmdq.dto.LoginRequest;
import com.example.movilidadmdq.dto.RegistroRequest;
import com.example.movilidadmdq.dto.UsuarioResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.repository.ViajeRepository;
import com.example.movilidadmdq.model.Viaje;
import com.example.movilidadmdq.service.UsuarioService;
import com.example.movilidadmdq.service.HistorialViajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**

Este controlador centraliza todas las operaciones relacionadas con el perfil del usuario.
Desde el registro y login, hasta la consulta de su historial de viajes.

SEGURIDAD:
Utiliza el objeto 'Authentication' de Spring Security para asegurar que un usuario
solo pueda ver o borrar sus propios datos (evitando que el Usuario A vea los
viajes del Usuario B).

*/

@Tag(name = "Usuarios", description = "Registro, login, perfil e historial de viajes del usuario.")
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController
{
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ViajeRepository viajeRepository;
    private final PasswordEncoder passwordEncoder;
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
        // Sin try/catch: si las credenciales son invalidas, Spring Security
        // tira BadCredentialsException y el GlobalExceptionHandler la traduce
        // a un 401. Lo unico que hace este metodo es orquestar.
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @Operation(summary = "Registrarse", description = "Se ingresan credenciales para registrarse, devuelve token")
    @ApiResponses(value ={
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
    public ResponseEntity<UsuarioResponse> obtenerUsuarioActual(Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuarioService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @Operation(summary = "Obtener el historial de un usuario segun ID", description = "Devuelve una lista como historial del usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Datos del usuario"),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido"),
        @ApiResponse(responseCode = "403", description = "No tenés permiso para ver el historial de otro usuario")
    })

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<ViajeHistorialResponse>> obtenerHistorial(@PathVariable Long id, Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .filter(usuario -> usuario.getId().equals(id))
                .map(usuario -> viajeRepository.findByUsuarioIdOrderByFechaHoraDesc(usuario.getId()).stream()
                        .map(historialViajeService::toResponse)
                        .toList())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
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
            Authentication authentication
    )
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .filter(usuario -> usuario.getId().equals(id))
                .flatMap(usuario -> viajeRepository.findById(viajeId)
                        .filter(viaje -> viaje.getUsuario().getId().equals(usuario.getId())))
                .map(viaje ->
                {
                    viajeRepository.delete(viaje);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.status(403).build());
    }

    @Operation(summary = "Obtener viaje frecuente de un usuario segun ID", description = "Obtener viaje frecuente del usuario de la base de datos")
    @ApiResponses(value ={
        @ApiResponse(responseCode = "200", description = "Viaje frecuente del usuario hallado con exito"),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido"),
        @ApiResponse(responseCode = "403", description = "No tenés permiso para ver los viajes de otro usuario")
    })

    @GetMapping("/{id}/viaje-frecuente")
    public ResponseEntity<ViajeFrecuenteResponse> obtenerViajeFrecuente(
            @PathVariable Long id,
            Authentication authentication
    )
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .filter(usuario -> usuario.getId().equals(id))
                .flatMap(usuario -> viajeRepository.findByUsuarioIdOrderByFechaHoraDesc(usuario.getId()).stream()
                        .collect(Collectors.groupingBy(
                                viaje -> viaje.getOrigen() + "||" + viaje.getDestino(),
                                Collectors.counting()
                        ))
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() > 2)
                        .max(Map.Entry.comparingByValue())
                        .map(entry ->
                        {
                            String[] partes = entry.getKey().split("\\|\\|", 2);
                            return new ViajeFrecuenteResponse(partes[0], partes[1], entry.getValue());
                        }))
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
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest datosNuevos, Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .filter(usuario -> usuario.getId().equals(id))
                .map(usuario ->
                {
                    usuario.setUsername(datosNuevos.username());
                    usuario.setEmail(datosNuevos.email());
                    if (datosNuevos.password() != null && !datosNuevos.password().isBlank())
                    {
                        usuario.setPassword(passwordEncoder.encode(datosNuevos.password()));
                    }
                    return ResponseEntity.ok(usuarioService.toResponse(usuarioRepository.save(usuario)));
                }).orElse(ResponseEntity.status(403).build());
    }

    @Operation(summary = "Buscar usuario por username o email", description = "Devuelve los datos de un usuario buscado. Solo accesible por administradores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    @GetMapping("/buscar")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> buscarUsuario(@RequestParam String query) {
        return usuarioRepository.findByUsername(query)
                .or(() -> usuarioRepository.findByEmail(query))
                .map(usuarioService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar cuenta de usuario", description = "Elimina permanentemente la cuenta del usuario. Los usuarios pueden eliminar su propia cuenta, y los administradores pueden eliminar cualquier cuenta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuenta eliminada con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permiso para eliminar esta cuenta")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long id, Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .filter(usuario -> usuario.getId().equals(id) || usuario.getRole() == com.example.movilidadmdq.enums.Role.ADMIN)
                .map(usuario -> {
                    usuarioService.eliminarUsuario(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.status(403).build());
    }
}
