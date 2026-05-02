package com.example.movilidadmdq.dto;

import jakarta.validation.constraints.NotBlank;

public record CalculoViajeRequest(@NotBlank String origen, @NotBlank String destino)
{
}
