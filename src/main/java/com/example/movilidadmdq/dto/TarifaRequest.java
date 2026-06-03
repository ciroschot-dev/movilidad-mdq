package com.example.movilidadmdq.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TarifaRequest {
    @Schema(description= "precio base de taxi", example = "3000" )
    BigDecimal precioBase;

    Schema(description="precio por km en taxi", example = "200")
    BigDecimal precioPorKm;
}
