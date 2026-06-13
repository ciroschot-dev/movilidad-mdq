package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* 
   CLASE: ViajeHistorialResponse
   
   Este DTO de tipo 'record' es la representación detallada de un viaje guardado.
   Se utiliza para enviarle al usuario su bitácora de movimientos y para la 
   auditoría del administrador.
   
   IMPORTANCIA:
   Contiene la "foto" del viaje en el momento que se realizó: precios de aquel entonces, 
   el transporte elegido y todos los metadatos geográficos para poder re-visualizar 
   el trayecto en un mapa.
*/
public record ViajeHistorialResponse(
        /* 
           ID único del registro en la tabla de viajes.
        */
        @Schema(description = "ID del viaje", example = "123")
        Long id,

        /* 
           Descripción textual del origen y destino (ej: "Plaza Mitre").
        */
        @Schema(description = "Origen del viaje", example = "Plaza Mitre")
        String origen,

        @Schema(description = "Destino del viaje", example = "Estadio José María Minella")
        String destino,

        /* 
           DATOS DEL TRAYECTO:
           Distancia en metros y tiempo en minutos obtenidos originalmente de Google Maps.
        */
        @Schema(description = "Distancia del viaje en metros", example = "8200")
        Long distanciaEnMetros,

        @Schema(description = "Tiempo estimado del viaje en minutos", example = "18")
        Integer tiempoEstimadoMin,

        /* 
           COMPARATIVA DE PRECIOS HISTÓRICA:
           Guarda cuánto salía cada opción en el momento de la consulta.
        */
        @Schema(description = "Precio estimado en taxi", example = "4800.00")
        BigDecimal precioTaxi,

        @Schema(description = "Precio estimado en Uber", example = "4200.00")
        BigDecimal precioUber,

        @Schema(description = "Precio estimado en Didi", example = "4100.00")
        BigDecimal precioDidi,

        /* 
           ELECCIÓN DEL USUARIO:
           Si el usuario seleccionó una opción para viajar, aquí se guarda cuál fue.
        */
        @Schema(description = "Tipo de transporte elegido", example = "UBER")
        String tipoElegido,

        /* 
           MARCA TEMPORAL:
           Fecha y hora exacta de la consulta. Crucial para el ordenamiento cronológico.
        */
        @Schema(description = "Fecha y hora en que se calculó el viaje", example = "2026-06-03T14:30:00")
        LocalDateTime fechaHora,

        /* 
           ESTADO DE FAVORITO:
           Indica si el usuario marcó este trayecto específico como preferido.
        */
        @Schema(description = "Indica si el viaje está marcado como favorito", example = "true")
        boolean favorito,

        /* 
           AUDITORÍA:
           Nombre del usuario que realizó el viaje. Se usa principalmente en el Panel de Admin.
        */
        @Schema(description = "Username del usuario que realizó el viaje", example = "juan.perez")
        String username,

        /* 
           METADATOS GEOGRÁFICOS COMPLETOS:
           Place IDs y Coordenadas. 
           Permiten que el Frontend reconstruya la ruta exacta en el mapa 
           sin necesidad de que el usuario vuelva a buscar las direcciones.
        */
        @Schema(description = "Place ID de origen")
        String origenPlaceId,
        @Schema(description = "Latitud de origen")
        Double origenLat,
        @Schema(description = "Longitud de origen")
        Double origenLng,
        @Schema(description = "Place ID de destino")
        String destinoPlaceId,
        @Schema(description = "Latitud de destino")
        Double destinoLat,
        @Schema(description = "Longitud de destino")
        Double destinoLng
) {}
