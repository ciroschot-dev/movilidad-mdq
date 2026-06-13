package com.example.movilidadmdq.security;

import jakarta.servlet.http.HttpServletResponse;
import com.example.movilidadmdq.config.JwtAuthFilter;
import com.example.movilidadmdq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.repository.TarifaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/* 
   CLASE: SecurityConfig
   
   Esta clase es la "Constitución" de la seguridad en nuestra app. Define quién puede 
   acceder a qué, cómo se manejan las sesiones y cómo se integra el login social (Google).
*/
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize en los controladores.
@RequiredArgsConstructor
public class SecurityConfig
{

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final UsuarioRepository usuarioRepository;

    // Dominios permitidos (CORS) leídos desde variables de entorno para seguridad.
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8080}")
    private List<String> allowedOrigins;

    // Credenciales del admin principal cargadas de forma segura desde el archivo .env.
    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    /* 
       MÉTODO: userDetailsService
       Define cómo Spring Security debe buscar a los usuarios cuando intentan loguearse.
    */
    @Bean
    public UserDetailsService userDetailsService()
    {
        return username -> usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    /* 
       MÉTODO: authenticationManager
       Configura el motor que procesa el login tradicional (usuario/contraseña).
    */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception
    {
        return config.getAuthenticationManager();
    }

    /* 
       MÉTODO: securityFilterChain (EL CORAZÓN)
       Define la cadena de filtros y reglas de acceso.
    */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
                // 1. Configuración de CORS: Permite que el frontend (React) hable con el backend.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // 2. CSRF: Desactivado porque usamos JWT (Stateless), lo que nos hace inmunes a este ataque.
                .csrf(AbstractHttpConfigurer::disable)
                
                // 3. Manejo de Errores de Autenticación: Si el token falla, devolvemos un JSON estandarizado (401).
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                        {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            String json = "{"
                                    + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                                    + "\"status\":401,"
                                    + "\"error\":\"Unauthorized\","
                                    + "\"message\":\"No autenticado\","
                                    + "\"path\":\"" + request.getRequestURI() + "\""
                                    + "}";
                            try (PrintWriter writer = response.getWriter()) { writer.write(json); }
                        })
                )
                
                // 4. Reglas de Autorización: Define qué rutas son públicas y cuáles privadas.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/usuarios/login", "/usuarios/registro", "/oauth2/**", 
                                "/error", "/swagger-ui/**", "/api-docs/**"
                        ).permitAll() // RUTAS PÚBLICAS
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN") // SOLO ADMINISTRADORES
                        .anyRequest().authenticated() // TODO LO DEMÁS REQUIERE LOGIN
                )
                
                // 5. Gestión de Sesión: Ponemos STATELESS para que Spring no use Cookies. Cada petición debe traer su Token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 6. El Filtro JWT: Inyectamos nuestro filtro ANTES del filtro estándar de login.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                
                // 7. OAuth2 Login: Configura la entrada con Google.
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }

    /* 
       MÉTODO: initData (BOOTSTRAP DEL SISTEMA)
       Este código corre automáticamente al arrancar la aplicación.
       - Crea el usuario ADMIN si la base de datos está vacía.
       - Carga las tarifas base del taxi (sistema de fichas de Mar del Plata).
    */
    @Bean
    CommandLineRunner initData(UsuarioRepository userRepo, TarifaRepository tarifaRepo, PasswordEncoder encoder)
    {
        return args ->
        {
            // Verificación de Admin existente para evitar duplicados en reinicios.
            if (!userRepo.existsByRole(Role.ADMIN))
            {
                if (adminUsername != null && !adminUsername.isBlank()) {
                    Usuario admin = new Usuario();
                    admin.setUsername(adminUsername);
                    admin.setPassword(encoder.encode(adminPassword));
                    admin.setEmail("admin@movilidadmdq.com");
                    admin.setRole(Role.ADMIN);
                    userRepo.save(admin);
                    System.out.println("--- [SISTEMA] Usuario admin inicializado ---");
                }
            }

            // Inicialización de tarifas si la tabla está vacía.
            if (tarifaRepo.count() == 0)
            {
                Tarifa taxi = new Tarifa();
                taxi.setTipoTransporte(TipoTransporte.TAXI);
                taxi.setBajadaBanderaDia(new BigDecimal("2250.00"));
                taxi.setValorFichaDia(new BigDecimal("150.00"));
                taxi.setMetrosPorFicha(160);
                tarifaRepo.save(taxi);
                
                Tarifa uber = new Tarifa();
                uber.setTipoTransporte(TipoTransporte.UBER);
                tarifaRepo.save(uber);

                Tarifa didi = new Tarifa();
                didi.setTipoTransporte(TipoTransporte.DIDI);
                tarifaRepo.save(didi);
                System.out.println("--- [SISTEMA] Tarifas base cargadas ---");
            }
        };
    }

    /* 
       MÉTODO: corsConfigurationSource
       Define los permisos para que navegadores externos puedan acceder a nuestra API.
       Es vital para que React (Frontend) pueda comunicarse con Spring (Backend).
    */
    @Bean
    CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Origin", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
