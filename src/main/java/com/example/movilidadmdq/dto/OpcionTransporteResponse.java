package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.TipoTransporte;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class OpcionTransporteResponse
{
    private TipoTransporte tipo;
    private BigDecimal precioMin;
    private BigDecimal precioMax;
    private int tiempoMinutos;
}
