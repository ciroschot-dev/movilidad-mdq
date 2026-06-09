package com.example.movilidadmdq.model;

import com.example.movilidadmdq.enums.TipoTransporte;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tarifas")
public class Tarifa
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private TipoTransporte tipoTransporte;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precioBase;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precioPorKm;

    // Campos especificos para el modelo de fichas del taxi (Mar del Plata)
    // Nullable para mantener compatibilidad con filas UBER/DIDI
    @DecimalMin("0.0")
    private BigDecimal bajadaBanderaDia;

    @DecimalMin("0.0")
    private BigDecimal bajadaBanderaNoche;

    @DecimalMin("0.0")
    private BigDecimal valorFichaDia;

    @DecimalMin("0.0")
    private BigDecimal valorFichaNoche;

    private Integer metrosPorFicha;

    private LocalDateTime ultimaActualizacion;

    @PrePersist
    @PreUpdate
    public void preUpdate()
    {
        this.ultimaActualizacion = LocalDateTime.now();
    }
}