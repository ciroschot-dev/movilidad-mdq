package com.example.movilidadmdq.model;

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

public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String tipoTransporte;

    @NotNull @DecimalMin("0.0")
    private BigDecimal precioBase;

    @NotNull @DecimalMin("0.0")
    private BigDecimal precioPorKm;

    @NotNull @DecimalMin("0.0")
    private BigDecimal precioPorMinutoEspera;

    private LocalDateTime ultimaActualizacion;

    @PrePersist @PreUpdate
    public void preUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }
}