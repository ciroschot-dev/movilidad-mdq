package com.example.movilidadmdq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CalculoViajeRequest(
    @NotBlank String origen, 
    @NotBlank String destino,
    @NotNull Long usuarioId
)
{
}
