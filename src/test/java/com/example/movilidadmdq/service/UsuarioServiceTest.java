package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.AuthResponse;
import com.example.movilidadmdq.dto.RegistroRequest;
import com.example.movilidadmdq.exception.RecursoDuplicadoException;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void testRegistrarUsuarioExitoso() {
        // GIVEN
        RegistroRequest request = new RegistroRequest("nuevoUser", "test@test.com", "password123");
        
        when(usuarioRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPass");
        
        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setId(1L);
        usuarioGuardado.setUsername(request.username());
        usuarioGuardado.setEmail(request.email());
        
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake-jwt-token");

        // WHEN
        AuthResponse response = usuarioService.registrar(request);

        // THEN
        assertNotNull(response);
        assertEquals("nuevoUser", response.username());
        assertEquals("fake-jwt-token", response.token());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testRegistrarUsuarioDuplicadoDeberiaLanzarExcepcion() {
        // GIVEN
        RegistroRequest request = new RegistroRequest("existente", "test@test.com", "pass");
        when(usuarioRepository.findByUsername("existente")).thenReturn(Optional.of(new Usuario()));

        // WHEN & THEN
        assertThrows(RecursoDuplicadoException.class, () -> usuarioService.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }
}
