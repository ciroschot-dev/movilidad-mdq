package com.example.movilidadmdq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class MovilidadMdqApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(MovilidadMdqApplication.class, args);
    }

}
