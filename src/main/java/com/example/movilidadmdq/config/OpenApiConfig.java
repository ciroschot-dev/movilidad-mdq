package com.example.movilidadmdq.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI movilidadMDQOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API MovilidadMDQ")
                        .description("Servicio para la comparación de precios de transporte en Mar del Plata.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Soporte MovilidadMDQ")
                                .email("soporte@movilidadmdq.com")));
    }
}
