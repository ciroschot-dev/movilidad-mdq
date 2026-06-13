package com.example.movilidadmdq.security;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/* 
   CLASE: CustomOAuth2UserService
   
   Esta clase extiende el servicio base de Spring para OAuth2. Su misión es 
   personalizar el proceso de obtención de datos del usuario desde Google.
   
   LÓGICA ESTRATÉGICA:
   Implementa el "Just-In-Time Provisioning" (Creación justo a tiempo). Si un 
   usuario de Google entra por primera vez, el sistema le crea una cuenta 
   local automáticamente sin que tenga que llenar un formulario de registro.
*/
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService
{

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /* 
       MÉTODO: loadUser
       Se ejecuta cuando el usuario termina de poner su clave en la ventanita de Google.
    */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
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
        }

        return oAuth2User;
    }
}
