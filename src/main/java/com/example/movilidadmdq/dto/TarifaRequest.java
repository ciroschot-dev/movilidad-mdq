package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Lo que manda el administrador para actualizar las tarifas del taxi.
 * <p>
 * A diferencia de los otros DTOs, permite campos en null para hacer
 * actualizaciones parciales: si el admin solo quiere cambiar la ficha nocturna,
 * manda solo ese campo y el resto queda igual en la base.
 */
@Data
public class TarifaRequest {
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
