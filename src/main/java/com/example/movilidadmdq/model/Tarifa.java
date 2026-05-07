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

    private LocalDateTime ultimaActualizacion;

    @PrePersist
    @PreUpdate
    public void preUpdate()
    {
        this.ultimaActualizacion = LocalDateTime.now();
    }
}