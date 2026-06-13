package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.ActualizarUsuarioRequest;
import com.example.movilidadmdq.dto.LoginRequest;
import com.example.movilidadmdq.dto.AuthResponse;
import com.example.movilidadmdq.dto.RegistroRequest;
import com.example.movilidadmdq.dto.UsuarioResponse;
import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.exception.OperacionNoPermitidaException;
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
public class UsuarioService
{
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // Si authenticate() no tiro excepcion, Spring Security ya valido que
        // el usuario existe y la password es correcta. Confiamos en eso y solo
        // recuperamos la entidad para armar el AuthResponse.
        Usuario usuario = usuarioRepository.findByUsername(request.username()).orElseThrow();
        return toAuthResponse(usuario);
    }

    public AuthResponse registrar(RegistroRequest request)
    {
        // Username y email son unicos: si ya existen, es un conflicto (409),
        // no un error de validacion. El handler global se encarga del status.
        if (usuarioRepository.findByUsername(request.username()).isPresent())
        {
            throw new RecursoDuplicadoException("El username ya esta registrado");
        }
        if (usuarioRepository.findByEmail(request.email()).isPresent())
        {
            throw new RecursoDuplicadoException("El email ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRole(Role.USER);

        return toAuthResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse toResponse(Usuario usuario)
    {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getEmail(), usuario.getRole());
    }

    public void eliminarUsuario(Long id)
    {
        usuarioRepository.findById(id)
                .ifPresentOrElse(
                        usuarioRepository::delete,
                        () ->
                        {
                            throw new RecursoNoEncontradoException("Usuario no encontrado");
                        }
                );
    }

    /**
     * Actualiza el perfil del usuario autenticado. El {@code id} del path debe
     * coincidir con la identidad logueada: nadie edita el perfil de otro.
     * La password solo se reemplaza (y reencripta) si viene una nueva.
     */
    public UsuarioResponse actualizarPerfil(Usuario usuario, Long id, ActualizarUsuarioRequest datos)
    {
        verificarIdentidad(usuario, id);

        usuario.setUsername(datos.username());
        usuario.setEmail(datos.email());
        if (datos.password() != null && !datos.password().isBlank())
        {
            usuario.setPassword(passwordEncoder.encode(datos.password()));
        }
        return toResponse(usuarioRepository.save(usuario));
    }

    /**
     * Busca un usuario por username o email (uso admin). Si no existe, 404.
     */
    public UsuarioResponse buscar(String query)
    {
        return usuarioRepository.findByUsername(query)
                .or(() -> usuarioRepository.findByEmail(query))
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    /**
     * Elimina una cuenta. El usuario puede borrar la suya; un ADMIN puede borrar
     * cualquiera. En otro caso es una operación no permitida (403).
     */
    public void eliminarCuenta(Usuario solicitante, Long id)
    {
        boolean esPropia = solicitante.getId().equals(id);
        boolean esAdmin = solicitante.getRole() == Role.ADMIN;
        if (!esPropia && !esAdmin)
        {
            throw new OperacionNoPermitidaException("No tenés permiso para eliminar esta cuenta");
        }
        eliminarUsuario(id);
    }

    // Guard de pertenencia: el id del path tiene que ser el del usuario logueado.
    private void verificarIdentidad(Usuario usuario, Long id)
    {
        if (!usuario.getId().equals(id))
        {
            throw new OperacionNoPermitidaException("No tenés permiso para modificar los datos de otro usuario");
        }
    }

    private AuthResponse toAuthResponse(Usuario usuario)
    {
        String token = jwtService.generateToken(usuario);
        return new AuthResponse(usuario.getId(), usuario.getUsername(), usuario.getEmail(), token, usuario.getRole());
    }

}
