package com.example.movilidadmdq.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ViajeHistorialResponse(
        Long id,
        String origen,
        String destino,
        Long distanciaEnMetros,
        Integer tiempoEstimadoMin,
        BigDecimal precioTaxi,
        BigDecimal precioMinApp,
        BigDecimal precioMaxApp,
        LocalDateTime fechaHora
) {}
