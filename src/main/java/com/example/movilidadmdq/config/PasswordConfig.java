package com.example.movilidadmdq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provee la herramienta con la que la app encripta y verifica contraseñas.
 * <p>
 * Expone un único bean, {@link PasswordEncoder}, que el {@code UsuarioService}
 * usa al registrar (para guardar el hash en vez del texto plano) y al iniciar
 * sesión (para comparar). Sin este bean Spring Security no sabría cómo validar
 * contraseñas y la app no arrancaría.
 */
@Configuration
public class PasswordConfig
{
    /**
     * Bean que encripta y verifica contraseñas con BCrypt.
     * <p>
     * Elegimos BCrypt porque es de una sola vía (no se puede "des-encriptar") e
     * incluye un salt aleatorio: dos usuarios con la misma clave terminan con
     * hashes distintos, lo que corta los ataques con tablas precalculadas.
     */
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
