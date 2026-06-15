package com.example.movilidadmdq.security;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Personaliza la obtención de datos del usuario cuando entra con Google.
 * <p>
 * Extiende el servicio base de Spring para OAuth2 y aplica "creación justo a
 * tiempo": si alguien entra con Google por primera vez, le crea la cuenta local
 * en el momento, sin pedirle que complete un formulario de registro.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService
{

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Trae los datos del usuario desde Google y se asegura de que exista
     * localmente. Corre cuando la persona termina de autenticarse en Google.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
        log.info("""

        🔐 LOGIN GOOGLE - OBTENIENDO DATOS:
        -> 1. CustomOAuth2UserService.loadUser: Recibiendo datos de Google.
        -> 2. Verificando si el email existe en nuestra base de datos.
        """);

        // 1. Llamamos al proceso estándar de Spring para obtener los datos de Google.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. Extraemos el email (dato clave para identificar personas).
        String email = oAuth2User.getAttribute("email");

        if (email == null)
        {
            throw new OAuth2AuthenticationException("El proveedor OAuth2 no retornó un email");
        }

        // 3. Verificamos si ya existe un usuario local con ese correo en nuestra DB.
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        // 4. SI NO EXISTE: Procedemos a crear la cuenta local automáticamente.
        if (usuario == null)
        {
            usuario = new Usuario();
            usuario.setEmail(email);
            
            // Generamos un 'username' basado en la primera parte del correo.
            String baseUsername = email.split("@")[0];
            
            // Si el nombre de usuario ya está tomado, le agregamos un sufijo aleatorio.
            if (usuarioRepository.findByUsername(baseUsername).isPresent())
            {
                usuario.setUsername(baseUsername + "_" + UUID.randomUUID().toString().substring(0, 5));
            }
            else
            {
                usuario.setUsername(baseUsername);
            }
            
            // SEGURIDAD: Le asignamos una contraseña aleatoria compleja. 
            // Como entra por Google, no necesita saberla, pero la DB requiere que el campo no sea nulo.
            usuario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            
            // Por política de negocio, todos los ingresos por Google son 'USER'.
            usuario.setRole(Role.USER);
            
            // Guardamos el registro para que en el futuro el OAuth2SuccessHandler lo encuentre.
            usuarioRepository.save(usuario);
            log.info("-> Nuevo usuario registrado automáticamente mediante Google: {}", email);
        }

        return oAuth2User;
    }
}
