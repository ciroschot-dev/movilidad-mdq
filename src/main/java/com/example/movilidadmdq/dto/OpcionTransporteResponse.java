package com.example.movilidadmdq.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.movilidadmdq.enums.TipoTransporte;

import java.math.BigDecimal;

public record OpcionTransporteResponse(

        @Schema(description = "Tipo de transporte", example = "Uber")
        TipoTransporte tipo,

        @Schema(description = "Precio minimo del viaje", example = "3400")
        BigDecimal precioMin,

        @Schema(description = "Precio maximo del viaje", example = "6800")
        BigDecimal precioMax,

        @Schema(description = "Tiempo en minutos de llegada", example = "6")
        int tiempoMinutos,

        @Schema(description = "URL para redirigir a la opcion elegida", example = "www.uber.com/av%colon%1736")
        String url
) {
}
