package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TarifaRequest {
    @Schema(description = "Precio base del viaje en pesos", example = "1500.00")
    BigDecimal precioBase;

    @Schema(description = "Precio por kilómetro recorrido en pesos", example = "350.00")
    BigDecimal precioPorKm;
}
