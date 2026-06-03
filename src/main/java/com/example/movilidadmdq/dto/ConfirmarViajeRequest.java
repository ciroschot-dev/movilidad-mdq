package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.TipoTransporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO simplificado para evitar fallos de validación durante la depuración.
 */
public record ConfirmarViajeRequest(
        @NotBlank String origen,
        @NotBlank String destino,
        @NotNull Long distanciaEnMetros,
        @NotNull Integer tiempoEstimadoMin,
        @NotNull BigDecimal precioTaxi,
        @NotNull BigDecimal precioUberMin,
        @NotNull BigDecimal precioUberMax,
        @NotNull BigDecimal precioDidiMin,
        @NotNull BigDecimal precioDidiMax,
        @NotNull TipoTransporte tipoElegido
)
{
}
