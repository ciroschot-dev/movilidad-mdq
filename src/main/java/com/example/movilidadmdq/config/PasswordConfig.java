package com.example.movilidadmdq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig
{
    /**
     Definimos un "Bean" (un objeto manejado por Spring) que cualquier otra clase
     (como UsuarioService) puede pedir prestado para encriptar o verificar
     contraseñas.**/

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        // === LA ELECCIÓN DE SEGURIDAD: BCrypt ===
        // Retornamos una instancia de BCryptPasswordEncoder.
        // BCrypt es el estándar de la industria porque:
        // 1. Es un algoritmo de hashing (no se puede "des-encriptar").
        // 2. Incluye un "Salt" (semilla) aleatorio para evitar ataques de fuerza bruta.

        return new BCryptPasswordEncoder();
    }
}

/** ¿Qué hace esta clase en el proyecto?
  Su único trabajo es proveer una herramienta de "trituración" de contraseñas. Cuando un
  usuario se registra, el UsuarioService usa este passwordEncoder para transformar la clave
  "hola123" en algo como $2a$10$X5....

  2. ¿Por qué usar BCrypt y no guardar el texto plano?
   * Si hay un error de seguridad (hackeo): Si alguien entra a tu base de datos de AWS, solo
     verá los hashes raros. Como BCrypt es una función de una sola vía, el hacker no puede
     volver atrás para saber que el hash significa "hola123".
   * Seguridad contra "Tablas de Arcoiris": BCrypt genera un resultado distinto cada vez
     (gracias al Salt), incluso para la misma contraseña. Si dos usuarios eligen "123456", sus
     hashes en la base de datos serán totalmente diferentes.

  3. ¿Qué pasa si NO tenemos esta clase?
   * Spring Security no sabría cómo verificar las contraseñas durante el login y la aplicación
     daría un error al arrancar.
   * Sin esta configuración, estarías obligado a guardar las claves en texto plano, lo cual es
     una falla de seguridad gravísima que el profesor te marcaría seguro.**/




