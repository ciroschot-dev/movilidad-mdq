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

/* 
   CLASE: CustomOAuth2UserService
   
   Este servicio extiende el servicio base de Spring para OAuth2 y gestiona 
   la integración con Google.
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService
{

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /* 
       MÉTODO: loadUser
       Se ejecuta cuando el usuario termina de autenticarse en la ventanita de Google.
    */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
        log.info("""
        
        🌍 INICIANDO FLUJO LOGIN GOOGLE:
        -> 1. CustomOAuth2UserService.loadUser: Recibiendo datos de Google.
        -> 2. Verificando si el email existe en nuestra base de datos.
        """);

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");

        if (email == null)
        {
            throw new OAuth2AuthenticationException("El proveedor OAuth2 no retornó un email");
        }

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null)
        {
            usuario = new Usuario();
            usuario.setEmail(email);
            
            String baseUsername = email.split("@")[0];
            
            if (usuarioRepository.findByUsername(baseUsername).isPresent())
            {
                usuario.setUsername(baseUsername + "_" + UUID.randomUUID().toString().substring(0, 5));
            }
            else
            {
                usuario.setUsername(baseUsername);
            }
            
            usuario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            usuario.setRole(Role.USER);
            
            usuarioRepository.save(usuario);
            log.info("-> Nuevo usuario registrado automáticamente mediante Google: {}", email);
        }

        return oAuth2User;
    }
}
