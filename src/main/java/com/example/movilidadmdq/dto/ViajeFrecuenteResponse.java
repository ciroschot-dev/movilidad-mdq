package com.example.movilidadmdq.dto;

public record ViajeFrecuenteResponse(
        String origen,
        String destino,
        Long cantidad
) {
}