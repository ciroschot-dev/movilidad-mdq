package com.example.movilidadmdq.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.movilidadmdq.enums.TipoTransporte;

import java.math.BigDecimal;

public record OpcionTransporteResponse(

        @Schema(description = "Tipo de transporte", example = "Uber")
        TipoTransporte tipo,

        @Schema(description = "Precio estimado del viaje", example = "5200")
        BigDecimal precio,

        @Schema(description = "Tiempo en minutos de llegada", example = "6")
        int tiempoMinutos,

        @Schema(description = "Distancia del viaje en metros", example = "5000")
        long distanciaEnMetros,

        @Schema(description = "URL para redirigir a la opcion elegida", example = "www.uber.com/av%colon%1736")
        String url
) {
}
