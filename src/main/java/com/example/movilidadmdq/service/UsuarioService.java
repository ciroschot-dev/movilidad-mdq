package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.LoginRequest;
import com.example.movilidadmdq.dto.UsuarioResponse;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public Optional<UsuarioResponse> login(LoginRequest request) {
        return usuarioRepository.findByUsername(request.username())
                .filter(u -> u.getPassword().equals(request.password())) // Simplificado para demo
                .map(u -> new UsuarioResponse(u.getId(), u.getUsername(), u.getEmail()));
    }
}
