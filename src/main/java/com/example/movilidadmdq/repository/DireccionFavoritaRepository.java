package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.model.DireccionFavorita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a la base de datos para las direcciones favoritas.
 * <p>
 * Son los lugares que el usuario guarda con un alias (ej: "Casa", "Trabajo").
 * Viven aparte del historial de viajes para que el usuario los gestione por su
 * cuenta.
 */
@Repository
public interface DireccionFavoritaRepository extends JpaRepository<DireccionFavorita, Long> {

    /** Todas las direcciones guardadas por un usuario. */
    List<DireccionFavorita> findByUsuarioId(Long usuarioId);

    /** Busca por texto de dirección. Se usa para no guardar duplicados. */
    Optional<DireccionFavorita> findByUsuarioIdAndDireccion(Long usuarioId, String direccion);

    /**
     * Busca por el place id de Google.
     * <p>
     * Es la forma más confiable de detectar duplicados: el place id no cambia
     * aunque cambie el texto de la calle.
     */
    Optional<DireccionFavorita> findByUsuarioIdAndPlaceId(Long usuarioId, String placeId);
}
