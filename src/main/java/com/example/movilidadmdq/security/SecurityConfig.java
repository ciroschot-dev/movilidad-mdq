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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig
{

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8080,https://movilidad-mdq.vercel.app,https://movilidad-mb6kktce3-mdp-tech.vercel.app}")
    private List<String> allowedOrigins;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Bean
    public UserDetailsService userDetailsService()
    {
        return username -> usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception
    {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        // Cuando llega un request sin token o con token invalido a un
                        // endpoint protegido, Spring Security corta antes de llegar al
                        // controller. Por defecto devolveria un HTML feo o un 401 vacio:
                        // forzamos un JSON con el mismo shape de ApiError para que el
                        // cliente reciba siempre la misma forma de error.
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
                                    + "\"path\":\"" + request.getRequestURI() + "\","
                                    + "\"errores\":null"
                                    + "}";
                            try (PrintWriter writer = response.getWriter())
                            {
                                writer.write(json);
                            }
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/usuarios/login",
                                "/usuarios/registro",
                                "/oauth2/**",
                                "/login/**",
                                "/error",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/api-docs"
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }

    // Bootstrap inicial del sistema. Corre en cada arranque, pero:
    //
    //   - Si YA existe algun usuario con rol ADMIN en la DB, no se hace nada:
    //     la app arranca normal, las variables APP_ADMIN_USERNAME / _PASSWORD
    //     pueden estar vacias.
    //   - Si NO existe ningun admin (primer arranque contra una DB vacia),
    //     ahi si se exigen las variables y se crea el admin con esos valores.
    //   - Una vez creado, la password del admin vive en la tabla "usuarios"
    //     hasheada con BCrypt. Esa es la fuente de verdad: cambiar el .env
    //     despues NO modifica al admin existente.
    //   - Para cambiar la password real, se usa el endpoint PUT /usuarios/{id}
    //     o un UPDATE en la DB con un hash BCrypt nuevo.
    //
    // Las credenciales nunca van hardcodeadas en el codigo.
    @Bean
    CommandLineRunner initData(UsuarioRepository userRepo, TarifaRepository tarifaRepo, PasswordEncoder encoder)
    {
        return args ->
        {
            // 1. Asegurar Admin solo si no hay ninguno en la DB.
            if (!userRepo.existsByRole(Role.ADMIN))
            {
                if (adminUsername == null || adminUsername.isBlank()
                        || adminPassword == null || adminPassword.isBlank())
                {
                    throw new IllegalStateException(
                            "Primer arranque sin admin en la DB: APP_ADMIN_USERNAME y "
                                    + "APP_ADMIN_PASSWORD deben estar definidas en el entorno (.env)");
                }

                Usuario admin = new Usuario();
                admin.setUsername(adminUsername);
                admin.setPassword(encoder.encode(adminPassword));
                admin.setEmail("admin@movilidadmdq.com");
                admin.setRole(Role.ADMIN);
                userRepo.save(admin);
                System.out.println("--- [SISTEMA] Usuario admin creado ---");
            }

            // 2. Asegurar Tarifas
            if (tarifaRepo.count() == 0)
            {
                Tarifa taxi = new Tarifa();
                taxi.setTipoTransporte(TipoTransporte.TAXI);
                taxi.setBajadaBanderaDia(new BigDecimal("2250.00"));
                taxi.setBajadaBanderaNoche(new BigDecimal("2700.00"));
                taxi.setValorFichaDia(new BigDecimal("150.00"));
                taxi.setValorFichaNoche(new BigDecimal("180.00"));
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

    @Bean
    CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Origin", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
