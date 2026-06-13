package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**


        Esta clase es un DTO (Data Transfer Object) definido como un 'record' (introducido en
        Java 14/16).
        Los 'records' son clases inmutables ideales para transportar datos, ya que generan
        automáticamente los métodos equals, hashCode y el constructor.

        SU FUNCIÓN:
        Se utiliza específicamente en el proceso de edición de una dirección favorita.
        Permite que el frontend envíe únicamente el nuevo nombre (ej: cambiar "Casa" por
        "Hogar")
        sin necesidad de enviar las coordenadas o la dirección completa nuevamente.
   */

public record ActualizarDireccionFavoritaRequest(
        @Schema(description = "Nuevo nombre personalizado", example = "Trabajo")
        @NotBlank(message = "El nombre no puede estar vacío")
        String nombre
) {}
