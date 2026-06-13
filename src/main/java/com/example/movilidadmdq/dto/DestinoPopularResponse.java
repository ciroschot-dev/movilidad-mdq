package com.example.movilidadmdq.dto;

/**

      Este DTO de tipo 'record' se utiliza exclusivamente para la funcionalidad de
      estadísticas.
      Representa un par de datos: un lugar de destino y cuántas veces ha sido solicitado.

      SU FUNCIÓN:
      Permite al Frontend construir gráficos o listas de "Tendencias" (ej: el Dashboard de
      Admin).
      Es el resultado de una consulta SQL de tipo 'GROUP BY' realizada en el repositorio de
      viajes.
*/



public record DestinoPopularResponse(
    String destino,
    Long cantidad
) {}
