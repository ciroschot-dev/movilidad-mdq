package com.example.movilidadmdq.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Viaje
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    @Column(nullable = false)
    private Long distanciaEnMetros;

    @Column(nullable = false)
    private Integer tiempoEstimadoMin;

    @Column(nullable = false)
    private BigDecimal precioTaxi;

    @Column(nullable = false)
    private BigDecimal precioMinApp;

    @Column(nullable = false)
    private BigDecimal precioMaxApp;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}