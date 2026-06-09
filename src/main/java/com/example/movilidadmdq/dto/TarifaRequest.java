package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TarifaRequest {
    @Schema(description = "Precio base del viaje en pesos (legacy, modelo lineal)", example = "1500.00")
    BigDecimal precioBase;

    @Schema(description = "Precio por kilómetro recorrido en pesos (legacy, modelo lineal)", example = "350.00")
    BigDecimal precioPorKm;

    @Schema(description = "Bajada de bandera diurna (6:00 a 22:00)", example = "2250.00")
    BigDecimal bajadaBanderaDia;

    @Schema(description = "Bajada de bandera nocturna (22:00 a 6:00)", example = "2700.00")
    BigDecimal bajadaBanderaNoche;

    @Schema(description = "Valor de cada ficha en horario diurno", example = "150.00")
    BigDecimal valorFichaDia;

    @Schema(description = "Valor de cada ficha en horario nocturno", example = "180.00")
    BigDecimal valorFichaNoche;

    @Schema(description = "Cantidad de metros que equivalen a una ficha", example = "160")
    Integer metrosPorFicha;
}
