package com.example.movilidadmdq.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TarifaRequest {
    BigDecimal precioBase;
    BigDecimal precioPorKm;
}
