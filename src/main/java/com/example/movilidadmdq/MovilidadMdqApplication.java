package com.example.movilidadmdq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class MovilidadMdqApplication
{

    public static void main(String[] args)
    {


        // 1. Cargamos el archivo .env
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // Para que no explote si no está (aunque lo necesitamos)
                .load();

        // 2. Pasamos las variables del .env al sistema para que Spring las vea
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });



        SpringApplication.run(MovilidadMdqApplication.class, args);
    }

}
