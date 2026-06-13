package com.example.movilidadmdq.config;

import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**

Esta clase es un componente de seguridad que actúa como un filtro "puente".
Su función es interceptar CADA petición HTTP que llega al servidor para verificar
si el usuario tiene un Token JWT válido.

¿POR QUÉ HEREDA DE OncePerRequestFilter?:
Para garantizar que el filtro se ejecute exactamente una vez por cada petición
del usuario, evitando procesar el token múltiples veces en flujos complejos.

*/

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter
{
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException
    {
        /* === 1. EXTRACCIÓN Y VALIDACIÓN INICIAL DEL HEADER ===
        Buscamos el token en los encabezados. Si no existe o no es tipo
        "Bearer", dejamos que el pedido siga su camino sin autenticar al usuario.*/

        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            filterChain.doFilter(request, response);
            return;
        }

        // === 2. PROCESAMIENTO DEL TOKEN ===
        // Si llegamos aquí, hay un token. Lo extraemos (quitando la palabra 'Bearer ')

        jwtToken = authHeader.substring(7);

        try
        {
            // Intentamos obtener el usuario que está "adentro" del token.

            username = jwtService.extractUsername(jwtToken);

            // === 3. VERIFICACIÓN DE ESTADO Y AUTENTICACIÓN ===
            // Solo intentamos autenticar si el token trae un usuario
            // y si el sistema aún no tiene a nadie autenticado para esta petición.


                    var currentAuth = SecurityContextHolder.getContext().getAuthentication();
            if (username != null && (currentAuth == null || currentAuth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken))
            {
                // Sincronizamos con nuestra base de datos para obtener los datos reales del usuario.

                var userOpt = usuarioRepository.findByUsername(username);
                if (userOpt.isPresent())
                {
                    UserDetails userDetails = userOpt.get();

                    // Verificamos que el token no haya expirado y pertenezca al usuario encontrado.

                    if (jwtService.isTokenValid(jwtToken, userDetails))
                    {
                        // === 4. CREACIÓN DE LA IDENTIDAD EN SPRING SECURITY ===
                        // Si todo es válido, creamos un objeto de autenticación
                        // con los roles (authorities) del usuario.

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // "Sellemos" el pedido: A partir de esta línea, el usuario está logueado para el resto del flujo.



                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        }
        catch (Exception e)
        {
            // === 5. MANEJO DE ERRORES (FALLO SEGURO) ===
            // Si el token está corrupto, expirado o es falso, lanzará una excepción.
            // Lo que hacemos es limpiar cualquier rastro de identidad para que el pedido sea tratado como "Anónimo".

            SecurityContextHolder.clearContext();
        }
        // === 6. FINALIZACIÓN ===
        // Pase lo que pase, el filtro debe dejar que el pedido continúe hacia el Controlador solicitado.


        filterChain.doFilter(request, response);
    }
}

/*¿Qué pasa si hay un error (Punto 5)?: Es lo más importante. Si el token falla, el
     catch asegura que no quede ninguna sesión activa (clearContext). La petición NO
     se detiene ahí, pero seguirá su camino como un "desconocido". Luego, el sistema
     de seguridad lo rebotará con un 401 cuando intente entrar a una ruta privada.
   * ¿Por qué el Punto 6 es vital?: El comando filterChain.doFilter tiene que
     ejecutarse siempre. Si te olvidás de ponerlo o no se ejecuta, el navegador del
     usuario se quedaría "colgado" esperando una respuesta que nunca llega, porque el
     pedido se "perdió" dentro del filtro.
   * El concepto de "Sello" (Punto 4): Una vez que hacés el setAuthentication, ese
     usuario "existe" para los Controllers. Sin ese paso, aunque el token sea válido,
     el sistema no sabría quién es.*/
