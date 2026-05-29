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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final com.example.movilidadmdq.service.UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ViajeRepository viajeRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(usuarioService.login(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        try {
            return ResponseEntity.ok(usuarioService.registrar(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioActual(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuarioService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<ViajeHistorialResponse>> obtenerHistorial(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .filter(usuario -> usuario.getId().equals(id))
                .map(usuario -> viajeRepository.findByUsuarioIdOrderByFechaHoraDesc(usuario.getId()).stream()
                        .map(this::toHistorialResponse)
                        .toList())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }
    @DeleteMapping("/{id}/historial/{viajeId}")
    public ResponseEntity<Void> borrarViaje(
            @PathVariable Long id,
            @PathVariable Long viajeId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .filter(usuario -> usuario.getId().equals(id))
                .flatMap(usuario -> viajeRepository.findById(viajeId)
                        .filter(viaje -> viaje.getUsuario().getId().equals(usuario.getId())))
                .map(viaje -> {
                    viajeRepository.delete(viaje);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.status(403).build());
    }

    @GetMapping("/{id}/viaje-frecuente")
    public ResponseEntity<ViajeFrecuenteResponse> obtenerViajeFrecuente(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
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
                        .map(entry -> {
                            String[] partes = entry.getKey().split("\\|\\|", 2);
                            return new ViajeFrecuenteResponse(partes[0], partes[1], entry.getValue());
                        }))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    private ViajeHistorialResponse toHistorialResponse(Viaje viaje) {
        return new ViajeHistorialResponse(
                viaje.getId(),
                viaje.getOrigen(),
                viaje.getDestino(),
                viaje.getDistanciaEnMetros(),
                viaje.getTiempoEstimadoMin(),
                viaje.getPrecioTaxi(),
                viaje.getPrecioMinApp(),
                viaje.getPrecioMaxApp(),
                viaje.getFechaHora()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest datosNuevos, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .filter(usuario -> usuario.getId().equals(id))
                .map(usuario -> {
                    usuario.setUsername(datosNuevos.username());
                    usuario.setEmail(datosNuevos.email());
                    if (datosNuevos.password() != null && !datosNuevos.password().isBlank()) {
                        usuario.setPassword(passwordEncoder.encode(datosNuevos.password()));
                    }
                    return ResponseEntity.ok(usuarioService.toResponse(usuarioRepository.save(usuario)));
                }).orElse(ResponseEntity.status(403).build());
    }
}
