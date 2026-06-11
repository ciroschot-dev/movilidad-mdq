package com.example.movilidadmdq.enums;

/**
 * Las opciones de transporte que la app compara y ofrece.
 * <p>
 * Funciona como etiqueta en todo el dominio: identifica a qué medio
 * corresponde cada tarifa, qué eligió el usuario en un viaje y de qué
 * opción habla cada precio que devolvemos.
 */
public enum TipoTransporte
{
    /** Taxi tradicional: tarifa fija (bajada de bandera + fichas). */
    TAXI,

    /** Uber: precio por rango (mínimo y máximo segun demanda). */
    UBER,

    /** DiDi: precio por rango (mínimo y máximo segun demanda). */
    DIDI
}
