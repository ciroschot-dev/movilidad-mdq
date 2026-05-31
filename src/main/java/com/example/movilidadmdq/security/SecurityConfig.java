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

    @Bean
    public UserDetailsService userDetailsService()
    {
        return username -> usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
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

    @Bean
    CommandLineRunner initData(UsuarioRepository userRepo, TarifaRepository tarifaRepo, PasswordEncoder encoder) {
        return args -> {
            // 1. Asegurar Admin: Si existe lo actualiza, si no lo crea
            Usuario admin = userRepo.findByUsername("admin").orElse(null);
            if (admin == null) {
                admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setEmail("admin@movilidadmdq.com");
                admin.setRole(Role.ADMIN);
                userRepo.save(admin);
                System.out.println("--- [SISTEMA] Usuario admin creado (admin/admin123) ---");
            } else if (admin.getRole() != Role.ADMIN) {
                admin.setRole(Role.ADMIN);
                userRepo.save(admin);
                System.out.println("--- [SISTEMA] Usuario admin actualizado a rol ADMIN ---");
            }

            // 2. Asegurar Tarifas
            if (tarifaRepo.count() == 0) {
                tarifaRepo.save(new Tarifa(null, TipoTransporte.TAXI, new BigDecimal("2250.00"), new BigDecimal("937.50"), null));
                tarifaRepo.save(new Tarifa(null, TipoTransporte.UBER, BigDecimal.ZERO, BigDecimal.ZERO, null));
                tarifaRepo.save(new Tarifa(null, TipoTransporte.DIDI, BigDecimal.ZERO, BigDecimal.ZERO, null));
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
