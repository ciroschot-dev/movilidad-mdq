package com.example.movilidadmdq;

import com.example.movilidadmdq.repository.ViajeRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MovilidadMdqApplication
{

    public static void main(String[] args)
    {
        // 1. Cargamos el archivo .env
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // 2. Pasamos las variables del .env al sistema para que Spring las vea
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(MovilidadMdqApplication.class, args);
    }

    @Bean
    public CommandLineRunner testConnection(ViajeRepository repository) {
        return args -> {
            System.out.println("\n-------------------------------------------------");
            System.out.println("🔍 VERIFICANDO CONEXIÓN A AWS RDS...");
            
            try {
                repository.findAll().stream().findFirst().ifPresentOrElse(
                    viaje -> {
                        System.out.println("✅ CONEXIÓN EXITOSA!");
                        System.out.println("📍 Viaje encontrado:");
                        System.out.println("   - Origen: " + viaje.getOrigen());
                        System.out.println("   - Destino: " + viaje.getDestino());
                        System.out.println("   - Precio Taxi: $" + viaje.getPrecioTaxi());
                    },
                    () -> System.out.println("⚠️ Conexión OK, pero no hay viajes en la tabla.")
                );
            } catch (Exception e) {
                System.err.println("❌ ERROR AL CONECTAR CON AWS:");
                System.err.println("   " + e.getMessage());
            }
            
            System.out.println("-------------------------------------------------\n");
        };
    }

}
