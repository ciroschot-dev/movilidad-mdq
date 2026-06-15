package com.example.movilidadmdq.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un lugar que el usuario guardó con un alias para reusarlo rápido.
 * <p>
 * Por ejemplo "Casa" o "Trabajo": guardamos la dirección junto con el place id
 * y las coordenadas de Google para poder reabrirla en el mapa sin volver a
 * buscarla. Cada dirección favorita pertenece a un usuario.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "direcciones_favoritas")
public class DireccionFavorita
{
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
