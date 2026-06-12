package com.example.movilidadmdq.model;

import com.example.movilidadmdq.enums.TipoTransporte;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tarifa de un medio de transporte (Taxi, Uber o Didi).
 * <p>
 * Guarda los valores que se usan para calcular cuánto sale un viaje:
 * un precio base, un precio por kilómetro y -en el caso del taxi de
 * Mar del Plata- los valores del sistema de "fichas" (bajada de bandera
 * y costo de cada ficha, diferenciando día y noche).
 * <p>
 * Hay una fila por cada {@link TipoTransporte}.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tarifas")
public class Tarifa
{
    // === Identificador ===

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A qué medio de transporte pertenece esta tarifa (TAXI, UBER o DIDI).
     */
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private TipoTransporte tipoTransporte;

    // === Valores específicos del taxi de Mar del Plata (sistema de fichas) ===
    // Estos campos son opcionales para que las filas de Uber y Didi puedan
    // dejarlos vacíos sin romper la base de datos.

    /**
     * Bajada de bandera durante el día (tarifa diurna del taxi).
     */
    @DecimalMin("0.0")
    private BigDecimal bajadaBanderaDia;

    /**
     * Bajada de bandera durante la noche (tarifa nocturna del taxi).
     */
    @DecimalMin("0.0")
    private BigDecimal bajadaBanderaNoche;

    /**
     * Cuánto vale cada ficha durante el día.
     */
    @DecimalMin("0.0")
    private BigDecimal valorFichaDia;

    /**
     * Cuánto vale cada ficha durante la noche.
     */
    @DecimalMin("0.0")
    private BigDecimal valorFichaNoche;

    /**
     * Cada cuántos metros recorridos se cobra una ficha nueva.
     */
    private Integer metrosPorFicha;

    // === Auditoría ===

    /**
     * Fecha y hora de la última vez que se modificó esta tarifa.
     */
    private LocalDateTime ultimaActualizacion;

    /**
     * Actualiza automáticamente la fecha de modificación cada vez que la
     * tarifa se guarda o se edita en la base de datos.
     */
    @PrePersist
    @PreUpdate
    public void preUpdate()
    {
        this.ultimaActualizacion = LocalDateTime.now();
    }
}