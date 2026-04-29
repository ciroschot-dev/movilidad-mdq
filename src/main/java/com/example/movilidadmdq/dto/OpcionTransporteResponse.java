package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.TipoTransporte;

import java.math.BigDecimal;

public record OpcionTransporteResponse(
        TipoTransporte tipo,
        BigDecimal precioMin,
        BigDecimal precioMax,
        int tiempoMinutos,
        String url
) {
}
