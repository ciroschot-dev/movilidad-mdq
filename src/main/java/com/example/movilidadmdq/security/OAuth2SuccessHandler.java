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

/**
 * Se activa cuando alguien termina de loguearse con Google y hace de puente.
 * <p>
 * Google confirma que la persona es real; este handler la convierte en un
 * usuario de nuestra plataforma generándole un token JWT propio y redirigiéndolo
 * al frontend con ese token, para que pueda seguir usando la API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler
{

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    // Dirección del frontend (React) donde el usuario debe aterrizar tras el login.
    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/redirect}")
    private String redirectUri;

    /**
     * Corre apenas Google valida la identidad: si el usuario existe en nuestra
     * base le arma el token y lo manda al frontend; si no, redirige con un error.
     */
    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException, ServletException
    {
        log.info("""

        ✅ LOGIN GOOGLE EXITOSO:
        -> 3. OAuth2SuccessHandler.onAuthenticationSuccess: Identidad validada por Google.
        -> 4. JwtService.generateToken: Creando token JWT para el usuario.
        -> 5. Redirigiendo al Frontend con el token en la URL.
        """);

        // 1. Obtenemos los datos que nos envió Google (principalmente el email).
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // 2. Buscamos si ese correo ya existe en nuestra base de datos local.
        // NOTA: Para el login con Google, el usuario debe haber sido registrado previamente
        // o ser creado automáticamente (dependiendo de la lógica de negocio elegida).
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        // MANEJO DE CASO NO ENCONTRADO:
        // Si no lo encontramos, redirigimos al frontend con un parámetro de error.
        if (usuarioOpt.isEmpty())
        {
            String urlError = construirUrl(redirectUri, "error", "usuario_no_encontrado");
            getRedirectStrategy().sendRedirect(request, response, urlError);
            return;
        }

        // 3. GENERACIÓN DE IDENTIDAD PROPIA:
        // Como ya sabemos quién es en nuestra DB, le fabricamos un Token JWT.
        String token = jwtService.generateToken(usuarioOpt.get());

        // 4. REDIRECCIÓN EXITOSA: 
        // Mandamos al usuario al frontend inyectando el token en la URL (como query param).
        // El componente 'OAuth2RedirectHandler' en React lo capturará y guardará.
        String urlExito = construirUrl(redirectUri, "token", token);
        getRedirectStrategy().sendRedirect(request, response, urlExito);
    }

    // Concatena un parámetro a la URL de forma segura, codificando los
    // caracteres especiales del valor.
    private String construirUrl(String base, String clave, String valor)
    {
        String separador = base.contains("?") ? "&" : "?";
        String valorEncoded = URLEncoder.encode(valor, StandardCharsets.UTF_8);
        return base + separador + clave + "=" + valorEncoded;
    }
}
