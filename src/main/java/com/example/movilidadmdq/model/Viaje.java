package com.example.movilidadmdq.model;

import com.example.movilidadmdq.enums.TipoTransporte;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un viaje consultado por un usuario: de dónde a dónde, cuánto mide, cuánto
 * tarda y qué precio sale en cada app de transporte.
 * <p>
 * Es la pieza central del historial: cada vez que alguien calcula una ruta se
 * guarda una fila de esta tabla. También sirve para los favoritos, por eso
 * arrastra los datos del lugar (place id y coordenadas) que después usamos para
 * armar el deep link a Google Maps sin volver a buscar la dirección.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "viajes")
public class Viaje
{
    // === Identificador ===

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Datos de la ruta ===

    /**
     * Dirección de partida tal como la escribió el usuario.
     */
    @Column(nullable = false)
    private String origen;

    /**
     * Dirección de llegada tal como la escribió el usuario.
     */
    @Column(nullable = false)
    private String destino;

    /**
     * Distancia total de la ruta, en metros. Es la base para calcular precios.
     */
    @Column(nullable = false)
    private Long distanciaEnMetros;

    /**
     * Tiempo estimado del viaje, en minutos.
     */
    @Column(nullable = false)
    private Integer tiempoEstimadoMin;

    // === Precios por tipo de transporte ===
    // Un precio estimado por opción (taxi, Uber y Didi).

    /**
     * Precio del taxi (tarifa fija calculada por bajada de bandera + fichas).
     */
    @Column
    private BigDecimal precioTaxi;

    @Column
    private BigDecimal precioUber;

    @Column
    private BigDecimal precioDidi;

    // === Estado del viaje ===

    /**
     * Transporte que el usuario terminó eligiendo (taxi, uber, didi). Puede
     * quedar vacío si solo consultó precios sin decidirse.
     */
    @Enumerated(EnumType.STRING)
    @Column
    private TipoTransporte tipoElegido;

    /**
     * Momento en que se guardó el viaje. Se setea solo al crearlo y no se
     * vuelve a tocar (updatable = false).
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    /**
     * Marca si el usuario guardó este viaje como favorito.
     */
    @Column(nullable = false)
    private boolean favorito = false;

    // === Datos del lugar para deep links ===
    // El "place id" es el identificador único que Google le da a cada lugar.
    // Guardarlo junto con las coordenadas nos deja reabrir el viaje en Google
    // Maps (el "deep link") sin tener que geolocalizar de nuevo la dirección.

    private String origenPlaceId;
    private Double origenLat;
    private Double origenLng;
    private String destinoPlaceId;
    private Double destinoLat;
    private Double destinoLng;

    // === Dueño del viaje ===

    /**
     * Usuario que hizo esta consulta. Muchos viajes pertenecen a un usuario.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}