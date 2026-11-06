package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.TipoTransporte;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Representa una de las alternativas de transporte (Uber, Taxi o DiDi)
 * que se le muestran al usuario en la pantalla de cotización.
 */
public record OpcionTransporteResponse(

        @Schema(description = "Identificador del viaje guardado en el historial", example = "42")
        Long viajeId,

        @Schema(description = "Tipo de transporte", example = "UBER")
        TipoTransporte tipo,

        @Schema(description = "Precio estimado del viaje", example = "5200")
        BigDecimal precio,

        @Schema(description = "Tiempo en minutos de llegada", example = "6")
        int tiempoMinutos,

        @Schema(description = "Distancia del viaje en metros", example = "5000")
        long distanciaEnMetros,

        @Schema(description = "URL para redirigir a la opción elegida", example = "https://m.uber.com/ul/...")
        String url
) {
    /** Constructor sin viajeId, para cuando la opción se construye antes de persistir el viaje. */
    public OpcionTransporteResponse(TipoTransporte tipo, BigDecimal precio, int tiempoMinutos, long distanciaEnMetros, String url) {
        this(null, tipo, precio, tiempoMinutos, distanciaEnMetros, url);
    }

    /** Devuelve una copia de la opción con el ID de viaje asociado. */
    public OpcionTransporteResponse withViajeId(Long nuevoViajeId) {
        return new OpcionTransporteResponse(nuevoViajeId, tipo, precio, tiempoMinutos, distanciaEnMetros, url);
    }
}
