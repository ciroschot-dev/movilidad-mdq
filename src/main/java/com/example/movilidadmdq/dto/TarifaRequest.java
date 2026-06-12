package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

// Todos los campos son opcionales (el patch actualiza solo los que vienen
// distintos de null). Las validaciones se disparan unicamente si el campo
// llega con valor: no podemos aceptar precios o metros negativos.
@Data
public class TarifaRequest {
    @Schema(description = "Precio base del viaje en pesos (legacy, modelo lineal)", example = "1500.00")
    @PositiveOrZero(message = "El precio base no puede ser negativo")
    BigDecimal precioBase;

    @Schema(description = "Precio por kilómetro recorrido en pesos (legacy, modelo lineal)", example = "350.00")
    @PositiveOrZero(message = "El precio por km no puede ser negativo")
    BigDecimal precioPorKm;

    @Schema(description = "Bajada de bandera diurna (6:00 a 22:00)", example = "2250.00")
    @PositiveOrZero(message = "La bajada de bandera diurna no puede ser negativa")
    BigDecimal bajadaBanderaDia;

    @Schema(description = "Bajada de bandera nocturna (22:00 a 6:00)", example = "2700.00")
    @PositiveOrZero(message = "La bajada de bandera nocturna no puede ser negativa")
    BigDecimal bajadaBanderaNoche;

    @Schema(description = "Valor de cada ficha en horario diurno", example = "150.00")
    @PositiveOrZero(message = "El valor de la ficha diurna no puede ser negativo")
    BigDecimal valorFichaDia;

    @Schema(description = "Valor de cada ficha en horario nocturno", example = "180.00")
    @PositiveOrZero(message = "El valor de la ficha nocturna no puede ser negativo")
    BigDecimal valorFichaNoche;

    @Schema(description = "Cantidad de metros que equivalen a una ficha", example = "160")
    @Positive(message = "Los metros por ficha deben ser mayores a cero")
    Integer metrosPorFicha;
}
