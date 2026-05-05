package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.UsuarioResponse;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.repository.ViajeRepository;
import com.example.movilidadmdq.model.Viaje;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {
    private final com.example.movilidadmdq.service.UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ViajeRepository viajeRepository;

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@RequestBody com.example.movilidadmdq.dto.LoginRequest request) {
        return usuarioService.login(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/{id}/historial")
    public List<Viaje> obtenerHistorial(@PathVariable Long id) {
        // En una app real filtraríamos por ID en el repo, para demo mostramos todos
        return viajeRepository.findAll(); 
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@PathVariable Long id, @RequestBody Usuario datosNuevos) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setUsername(datosNuevos.getUsername());
            usuario.setEmail(datosNuevos.getEmail());
            if (datosNuevos.getPassword() != null && !datosNuevos.getPassword().isBlank()) {
                usuario.setPassword(datosNuevos.getPassword());
            }
            Usuario actualizado = usuarioRepository.save(usuario);
            return ResponseEntity.ok(new UsuarioResponse(actualizado.getId(), actualizado.getUsername(), actualizado.getEmail()));
        }).orElse(ResponseEntity.notFound().build());
    }
}
