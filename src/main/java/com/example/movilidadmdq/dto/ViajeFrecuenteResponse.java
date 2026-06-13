package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/* 
   CLASE: ViajeFrecuenteResponse
   
   Este DTO de tipo 'record' representa el resultado del algoritmo de detección 
   de patrones de movilidad del usuario. 
   
   SU FUNCIÓN:
   Informa al Frontend cuál es el trayecto (Origen-Destino) que el usuario repite 
   con más asiduidad. Esto permite que la interfaz ofrezca accesos directos o 
   sugerencias personalizadas, mejorando la experiencia de uso (UX).
*/
public record ViajeFrecuenteResponse(
        /* 
           Nombre o dirección del punto de partida más habitual.
        */
        @Schema(description = "Origen del viaje frecuente", example = "Plaza Mitre")
        String origen,

        /* 
           Nombre o dirección del punto de llegada más habitual.
        */
        @Schema(description = "Destino del viaje frecuente", example = "Estadio José María Minella")
        String destino,

        /* 
           Métrica de frecuencia: Indica el total de veces que este par 
           específico (Origen + Destino) fue consultado por el usuario.
           Se usa para justificar por qué se considera "frecuente".
        */
        @Schema(description = "Cantidad de veces que el usuario realizó este viaje", example = "5")
        Long cantidad
) {
}
