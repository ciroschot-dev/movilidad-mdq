package com.example.movilidadmdq.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "direcciones_favoritas")
public class DireccionFavorita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String nombre; // Alias personalizado (Casa, Trabajo, etc.)

    @Column(nullable = false)
    private String direccion;

    private String placeId;
    private Double lat;
    private Double lng;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
