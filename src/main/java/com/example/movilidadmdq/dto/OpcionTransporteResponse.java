package com.example.movilidadmdq.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.movilidadmdq.enums.TipoTransporte;

import java.math.BigDecimal;

/**


Esta clase es un DTO (Data Transfer Object) de tipo 'record'.
Su función es representar una de las alternativas de viaje (Uber, Taxi o Didi)
que se le muestran al usuario en la pantalla de resultados.

SU IMPORTANCIA:
Consolida la información proveniente de múltiples fuentes: el motor de cálculos,
la base de datos de tarifas y los servicios de Deep Linking.

*/

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
