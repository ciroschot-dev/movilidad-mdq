package com.example.movilidadmdq.security;

import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/* 
   CLASE: OAuth2SuccessHandler
   
   Componente que finaliza el proceso de login social. Genera nuestro Token JWT 
   y redirige al usuario hacia el Frontend.
*/
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler
{

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException, ServletException
    {
        log.info("""
        
        ✅ LOGIN GOOGLE EXITOSO:
        -> 3. OAuth2SuccessHandler.onAuthenticationSuccess: Identidad validada por Google.
        -> 4. JwtService.generateToken: Creando token JWT para el usuario.
        -> 5. Redirigiendo al Frontend con el token en la URL.
        """);

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isEmpty())
        {
            String urlError = construirUrl(redirectUri, "error", "usuario_no_encontrado");
            getRedirectStrategy().sendRedirect(request, response, urlError);
            return;
        }

        String token = jwtService.generateToken(usuarioOpt.get());

        String urlExito = construirUrl(redirectUri, "token", token);
        getRedirectStrategy().sendRedirect(request, response, urlExito);
    }

    private String construirUrl(String base, String clave, String valor)
    {
        String separador = base.contains("?") ? "&" : "?";
        String valorEncoded = URLEncoder.encode(valor, StandardCharsets.UTF_8);
        return base + separador + clave + "=" + valorEncoded;
    }
}
