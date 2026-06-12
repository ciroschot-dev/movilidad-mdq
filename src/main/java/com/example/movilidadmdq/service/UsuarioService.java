package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.LoginRequest;
import com.example.movilidadmdq.dto.AuthResponse;
import com.example.movilidadmdq.dto.RegistroRequest;
import com.example.movilidadmdq.dto.UsuarioResponse;
import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.exception.RecursoDuplicadoException;
import com.example.movilidadmdq.exception.RecursoNoEncontradoException;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // Si authenticate() no tiro excepcion, Spring Security ya valido que
        // el usuario existe y la password es correcta. Confiamos en eso y solo
        // recuperamos la entidad para armar el AuthResponse.
        Usuario usuario = usuarioRepository.findByUsername(request.username()).orElseThrow();
        return toAuthResponse(usuario);
    }

    public AuthResponse registrar(RegistroRequest request) {
        // Username y email son unicos: si ya existen, es un conflicto (409),
        // no un error de validacion. El handler global se encarga del status.
        if (usuarioRepository.findByUsername(request.username()).isPresent()) {
            throw new RecursoDuplicadoException("El username ya esta registrado");
        }
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new RecursoDuplicadoException("El email ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRole(Role.USER);

        return toAuthResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getEmail(), usuario.getRole());
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.findById(id)
                .ifPresentOrElse(
                        usuarioRepository::delete,
                        () -> { throw new RecursoNoEncontradoException("Usuario no encontrado"); }
                );
    }

    private AuthResponse toAuthResponse(Usuario usuario) {
        String token = jwtService.generateToken(usuario);
        return new AuthResponse(usuario.getId(), usuario.getUsername(), usuario.getEmail(), token, usuario.getRole());
    }

}
