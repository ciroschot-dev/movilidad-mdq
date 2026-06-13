package com.example.movilidadmdq.security;

import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
   
   Este componente se activa automáticamente cuando un usuario se loguea 
   con éxito a través de Google (OAuth2).
   
   SU FUNCIÓN:
   Actuar como puente. Google nos dice "esta persona es real", y nosotros 
   debemos transformarla en un usuario de nuestra plataforma enviándole 
   un Token JWT propio para que pueda seguir navegando en nuestra API.
*/
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler
{

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    // Dirección del frontend (React) donde el usuario debe aterrizar tras el login.
    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/redirect}")
    private String redirectUri;

    /* 
       MÉTODO: onAuthenticationSuccess
       Se ejecuta justo después de que el proveedor externo (Google) valida la identidad.
    */
    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException, ServletException
    {
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

    /* 
       MÉTODO AUXILIAR: construirUrl
       Se encarga de concatenar parámetros a la URL de forma segura, 
       manejando la codificación de caracteres especiales.
    */
    private String construirUrl(String base, String clave, String valor)
    {
        String separador = base.contains("?") ? "&" : "?";
        String valorEncoded = URLEncoder.encode(valor, StandardCharsets.UTF_8);
        return base + separador + clave + "=" + valorEncoded;
    }
}
