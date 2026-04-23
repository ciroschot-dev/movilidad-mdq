package com.example.movilidadmdq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data

public class CalculoViajeRequest
{
    @NotBlank
    private String origen;

    @NotBlank
    private String destino;
}
