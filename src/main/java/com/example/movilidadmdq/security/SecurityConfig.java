package com.example.movilidadmdq.security;

import jakarta.servlet.http.HttpServletResponse;
import com.example.movilidadmdq.config.JwtAuthFilter;
import com.example.movilidadmdq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
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
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                        )
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
    //   - El admin se crea SOLO si no existe en la DB. Las variables de entorno
    //     APP_ADMIN_USERNAME y APP_ADMIN_PASSWORD se usan UNICAMENTE en ese
    //     primer arranque contra una DB vacia.
    //   - Una vez creado, la password del admin vive en la tabla "usuarios"
    //     hasheada con BCrypt. Esa es la fuente de verdad: cambiar el .env
    //     despues NO modifica al admin existente.
    //   - Para cambiar la password real, se usa el endpoint PUT /usuarios/{id}
    //     o un UPDATE en la DB con un hash BCrypt nuevo.
    //
    // Las credenciales nunca van hardcodeadas en el codigo: si las variables
    // no estan seteadas, la app falla al arrancar.
    @Bean
    CommandLineRunner initData(UsuarioRepository userRepo, TarifaRepository tarifaRepo, PasswordEncoder encoder) {
        return args -> {
            if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException(
                        "APP_ADMIN_USERNAME y APP_ADMIN_PASSWORD deben estar definidas en el entorno (.env)");
            }

            // 1. Asegurar Admin: si no existe lo crea con las credenciales del .env.
            //    Si ya existe, solo verifica que tenga rol ADMIN (no toca la password).
            Usuario admin = userRepo.findByUsername(adminUsername).orElse(null);
            if (admin == null) {
                admin = new Usuario();
                admin.setUsername(adminUsername);
                admin.setPassword(encoder.encode(adminPassword));
                admin.setEmail("admin@movilidadmdq.com");
                admin.setRole(Role.ADMIN);
                userRepo.save(admin);
                System.out.println("--- [SISTEMA] Usuario admin creado ---");
            } else if (admin.getRole() != Role.ADMIN) {
                admin.setRole(Role.ADMIN);
                userRepo.save(admin);
                System.out.println("--- [SISTEMA] Usuario admin actualizado a rol ADMIN ---");
            }

            // 2. Asegurar Tarifas
            if (tarifaRepo.count() == 0) {
                Tarifa taxi = new Tarifa();
                taxi.setTipoTransporte(TipoTransporte.TAXI);
                taxi.setPrecioBase(new BigDecimal("2250.00"));
                taxi.setPrecioPorKm(new BigDecimal("937.50"));
                taxi.setBajadaBanderaDia(new BigDecimal("2250.00"));
                taxi.setBajadaBanderaNoche(new BigDecimal("2700.00"));
                taxi.setValorFichaDia(new BigDecimal("150.00"));
                taxi.setValorFichaNoche(new BigDecimal("180.00"));
                taxi.setMetrosPorFicha(160);
                tarifaRepo.save(taxi);

                Tarifa uber = new Tarifa();
                uber.setTipoTransporte(TipoTransporte.UBER);
                uber.setPrecioBase(BigDecimal.ZERO);
                uber.setPrecioPorKm(BigDecimal.ZERO);
                tarifaRepo.save(uber);

                Tarifa didi = new Tarifa();
                didi.setTipoTransporte(TipoTransporte.DIDI);
                didi.setPrecioBase(BigDecimal.ZERO);
                didi.setPrecioPorKm(BigDecimal.ZERO);
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
